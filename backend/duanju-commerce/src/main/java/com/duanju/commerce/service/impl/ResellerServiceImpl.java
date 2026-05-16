package com.duanju.commerce.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.commerce.domain.Reseller;
import com.duanju.commerce.domain.ResellerBind;
import com.duanju.commerce.domain.ResellerOrder;
import com.duanju.commerce.mapper.ResellerBindMapper;
import com.duanju.commerce.mapper.ResellerMapper;
import com.duanju.commerce.mapper.ResellerOrderMapper;
import com.duanju.commerce.service.ResellerService;
import com.duanju.commerce.service.WalletService;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.mapper.DramaUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResellerServiceImpl implements ResellerService {

    private final ResellerMapper resellerMapper;
    private final ResellerOrderMapper resellerOrderMapper;
    private final ResellerBindMapper resellerBindMapper;
    private final DramaUserMapper userMapper;
    private final WalletService walletService;

    @Autowired(required = false)
    private MessageSource messageSource;

    private String msg(String key) {
        if (messageSource == null) return key;
        try {
            return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    @Override
    public List<Reseller> listAll(Integer siteId) {
        return resellerMapper.selectList(new LambdaQueryWrapper<Reseller>()
                .eq(Reseller::getSiteId, siteId)
                .eq(Reseller::getStatus, "normal")
                .orderByDesc(Reseller::getWeigh));
    }

    @Override
    public Reseller getById(Long id) {
        return resellerMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResellerOrder createOrder(Integer siteId, Long userId, Long resellerId, String payType, String platform) {
        Reseller reseller = resellerMapper.selectById(resellerId);
        if (reseller == null || !"normal".equals(reseller.getStatus())) {
            throw ServiceException.of(msg("error.reseller.not.found"));
        }

        BigDecimal payFee = reseller.getPrice();

        ResellerOrder order = new ResellerOrder();
        order.setSiteId(siteId);
        order.setResellerId(resellerId);
        order.setOrderSn("RES" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setStatus(0);
        order.setTotalFee(reseller.getPrice());
        order.setPayFee(payFee);
        order.setPayType(payType);
        order.setPlatform(platform);
        resellerOrderMapper.insert(order);

        if ("wallet".equals(payType)) {
            walletService.deductWallet(siteId, userId, "money", payFee, "buy_reseller", "购买分销资格", order.getOrderSn());
            handlePaySuccess(order.getOrderSn(), null, null);
        }

        log.info("创建分销订单 userId={}, resellerId={}, orderSn={}", userId, resellerId, order.getOrderSn());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(String orderSn, String transactionId, String paymentJson) {
        ResellerOrder order = resellerOrderMapper.selectByOrderSn(orderSn);
        if (order == null || order.getStatus() != 0) return;

        Reseller reseller = resellerMapper.selectById(order.getResellerId());
        if (reseller == null) return;

        order.setStatus(1);
        order.setTransactionId(transactionId);
        order.setPaymentJson(paymentJson);
        order.setPayTime(LocalDateTime.now());
        resellerOrderMapper.updateById(order);

        // 更新用户分销资格
        DramaUser user = userMapper.selectById(order.getUserId());
        if (user == null) return;

        LocalDateTime base = (user.getResellerExpireTime() != null && user.getResellerExpireTime().isAfter(LocalDateTime.now()))
                ? user.getResellerExpireTime() : LocalDateTime.now();
        LocalDateTime newExpire = base.plusDays(reseller.getExpire());

        DramaUser update = new DramaUser();
        update.setId(user.getId());
        update.setResellerLevel(reseller.getLevel());
        update.setResellerExpireTime(newExpire);
        userMapper.updateById(update);

        // 发放上级佣金
        distributeCommission(order.getSiteId(), order.getUserId(), order.getPayFee(), reseller);

        log.info("分销支付成功 userId={}, orderSn={}, 到期={}", order.getUserId(), orderSn, newExpire);
    }

    private void distributeCommission(Integer siteId, Long userId, BigDecimal payFee, Reseller reseller) {
        DramaUser user = userMapper.selectById(userId);
        if (user == null || user.getParentId() == null) return;

        DramaUser parent = userMapper.selectById(user.getParentId());
        if (parent == null || !parent.isResellerActive()) return;

        // 直接上级佣金
        if (reseller.getDirect() != null && reseller.getDirect().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal commission = payFee.multiply(reseller.getDirect())
                    .divide(new BigDecimal(100), 2, RoundingMode.DOWN);
            walletService.addWallet(siteId, parent.getId(), "money", commission,
                    "reseller_commission", "分销佣金（直接）", userId.toString());
            log.info("发放直接佣金 parentId={}, amount={}", parent.getId(), commission);
        }

        // 间接上级佣金（二级）
        if (parent.getParentId() != null && reseller.getIndirect() != null
                && reseller.getIndirect().compareTo(BigDecimal.ZERO) > 0) {
            DramaUser grandParent = userMapper.selectById(parent.getParentId());
            if (grandParent != null && grandParent.isResellerActive()) {
                BigDecimal commission = payFee.multiply(reseller.getIndirect())
                        .divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                walletService.addWallet(siteId, grandParent.getId(), "money", commission,
                        "reseller_commission", "分销佣金（间接）", userId.toString());
                log.info("发放间接佣金 grandParentId={}, amount={}", grandParent.getId(), commission);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderSn) {
        ResellerOrder order = resellerOrderMapper.selectByOrderSn(orderSn);
        if (order == null || order.getStatus() != 0) return;
        order.setStatus(-1);
        resellerOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindInviter(Long userId, Long inviterUserId) {
        if (userId.equals(inviterUserId)) return;
        DramaUser user = userMapper.selectById(userId);
        if (user == null || user.getParentId() != null) return;

        DramaUser inviter = userMapper.selectById(inviterUserId);
        if (inviter == null) return;

        DramaUser update = new DramaUser();
        update.setId(userId);
        update.setParentId(inviterUserId);
        userMapper.updateById(update);
        log.info("绑定邀请关系 userId={}, inviterId={}", userId, inviterUserId);
    }
}
