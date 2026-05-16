package com.duanju.commerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.commerce.domain.Vip;
import com.duanju.commerce.domain.VipOrder;
import com.duanju.commerce.mapper.VipMapper;
import com.duanju.commerce.mapper.VipOrderMapper;
import com.duanju.commerce.service.VipService;
import com.duanju.commerce.service.WalletService;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.mapper.DramaUserMapper;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private final VipMapper vipMapper;
    private final VipOrderMapper vipOrderMapper;
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
    public List<Vip> listAll(Integer siteId) {
        return vipMapper.selectList(new LambdaQueryWrapper<Vip>()
                .eq(Vip::getSiteId, siteId)
                .eq(Vip::getStatus, "normal")
                .orderByDesc(Vip::getWeigh));
    }

    @Override
    public Vip getById(Long id) {
        return vipMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VipOrder createOrder(Integer siteId, Long userId, Long vipId, String payType, String platform) {
        Vip vip = vipMapper.selectById(vipId);
        if (vip == null || !"normal".equals(vip.getStatus())) {
            throw ServiceException.of(msg("error.vip.not.found"));
        }

        BigDecimal payFee = vip.getPrice();

        VipOrder order = new VipOrder();
        order.setSiteId(siteId);
        order.setVipId(vipId);
        order.setOrderSn("VIP" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setDays(vip.getDays());
        order.setStatus(0);
        order.setTotalFee(vip.getPrice());
        order.setPayFee(payFee);
        order.setPayType(payType);
        order.setPlatform(platform);
        vipOrderMapper.insert(order);

        // 余额支付直接处理
        if ("wallet".equals(payType)) {
            walletService.deductWallet(siteId, userId, "money", payFee, "buy_vip", "购买VIP", order.getOrderSn());
            handlePaySuccess(order.getOrderSn(), null, null);
        }

        log.info("创建VIP订单 userId={}, vipId={}, orderSn={}", userId, vipId, order.getOrderSn());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(String orderSn, String transactionId, String paymentJson) {
        VipOrder order = vipOrderMapper.selectByOrderSn(orderSn);
        if (order == null || order.getStatus() != 0) return;

        order.setStatus(1);
        order.setTransactionId(transactionId);
        order.setPaymentJson(paymentJson);
        order.setPayTime(LocalDateTime.now());
        vipOrderMapper.updateById(order);

        DramaUser user = userMapper.selectById(order.getUserId());
        if (user == null) return;

        LocalDateTime base = (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now()))
                ? user.getVipExpireTime() : LocalDateTime.now();
        LocalDateTime newExpire = base.plusDays(order.getDays());

        DramaUser update = new DramaUser();
        update.setId(user.getId());
        update.setVipExpireTime(newExpire);
        userMapper.updateById(update);

        log.info("VIP支付成功 userId={}, orderSn={}, 到期={}", order.getUserId(), orderSn, newExpire);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderSn) {
        VipOrder order = vipOrderMapper.selectByOrderSn(orderSn);
        if (order == null || order.getStatus() != 0) return;
        order.setStatus(-1);
        vipOrderMapper.updateById(order);
    }
}
