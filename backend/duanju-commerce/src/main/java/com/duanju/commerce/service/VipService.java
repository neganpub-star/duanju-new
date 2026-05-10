package com.duanju.commerce.service;

import com.duanju.commerce.domain.Vip;
import com.duanju.commerce.domain.VipOrder;

import java.util.List;

public interface VipService {
    List<Vip> listAll(Integer siteId);
    Vip getById(Long id);
    VipOrder createOrder(Integer siteId, Long userId, Long vipId, String payType, String platform);
    void handlePaySuccess(String orderSn, String transactionId, String paymentJson);
    void cancelOrder(String orderSn);
}
