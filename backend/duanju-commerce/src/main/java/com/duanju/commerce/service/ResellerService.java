package com.duanju.commerce.service;

import com.duanju.commerce.domain.Reseller;
import com.duanju.commerce.domain.ResellerOrder;

import java.util.List;

public interface ResellerService {
    List<Reseller> listAll(Integer siteId);
    Reseller getById(Long id);
    ResellerOrder createOrder(Integer siteId, Long userId, Long resellerId, String payType, String platform);
    void handlePaySuccess(String orderSn, String transactionId, String paymentJson);
    void cancelOrder(String orderSn);
    void bindInviter(Long userId, Long inviterUserId);
}
