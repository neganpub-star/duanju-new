package com.duanju.commerce.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.commerce.domain.Usable;
import com.duanju.commerce.domain.UsableOrder;
import com.duanju.commerce.mapper.UsableMapper;
import com.duanju.commerce.mapper.UsableOrderMapper;
import com.duanju.commerce.service.UsableService;
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
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsableServiceImpl implements UsableService {

    private final UsableMapper usableMapper;
    private final UsableOrderMapper usableOrderMapper;
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
    public List<Usable> listAll(Integer siteId) {
        return usableMapper.selectList(new LambdaQueryWrapper<Usable>()
                .eq(Usable::getSiteId, siteId)
                .eq(Usable::getStatus, "normal")
                .orderByDesc(Usable::getWeigh));
    }

    @Override
    public Usable getById(Long id) {
        return usableMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UsableOrder createOrder(Integer siteId, Long userId, Long usableId, String payType, String platform) {
        Usable usable = usableMapper.selectById(usableId);
        if (usable == null || !"normal".equals(usable.getStatus())) {
            throw ServiceException.of(msg("error.usable.not.found"));
        }

        BigDecimal payFee = usable.getPrice();

        UsableOrder order = new UsableOrder();
        order.setSiteId(siteId);
        order.setUsableId(usableId);
        order.setOrderSn("USE" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setUsable(usable.getUsable());
        order.setStatus(0);
        order.setTotalFee(usable.getPrice());
        order.setPayFee(payFee);
        order.setPayType(payType);
        order.setPlatform(platform);
        usableOrderMapper.insert(order);

        if ("wallet".equals(payType)) {
            walletService.deductWallet(siteId, userId, "money", payFee, "buy_usable", "购买点数", order.getOrderSn());
            handlePaySuccess(order.getOrderSn(), null, null);
        }

        log.info("创建点数订单 userId={}, usableId={}, orderSn={}", userId, usableId, order.getOrderSn());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(String orderSn, String transactionId, String paymentJson) {
        UsableOrder order = usableOrderMapper.selectByOrderSn(orderSn);
        if (order == null || order.getStatus() != 0) return;

        order.setStatus(1);
        order.setTransactionId(transactionId);
        order.setPaymentJson(paymentJson);
        order.setPayTime(LocalDateTime.now());
        usableOrderMapper.updateById(order);

        walletService.addWallet(order.getSiteId(), order.getUserId(), "usable",
                new BigDecimal(order.getUsable()), "buy_usable", "购买点数到账", orderSn);

        log.info("点数支付成功 userId={}, orderSn={}, 点数={}", order.getUserId(), orderSn, order.getUsable());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderSn) {
        UsableOrder order = usableOrderMapper.selectByOrderSn(orderSn);
        if (order == null || order.getStatus() != 0) return;
        order.setStatus(-1);
        usableOrderMapper.updateById(order);
    }
}
