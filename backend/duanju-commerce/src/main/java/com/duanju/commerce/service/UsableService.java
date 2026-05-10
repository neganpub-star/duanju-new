package com.duanju.commerce.service;

import com.duanju.commerce.domain.Usable;
import com.duanju.commerce.domain.UsableOrder;

import java.util.List;

public interface UsableService {
    List<Usable> listAll(Integer siteId);
    Usable getById(Long id);
    UsableOrder createOrder(Integer siteId, Long userId, Long usableId, String payType, String platform);
    void handlePaySuccess(String orderSn, String transactionId, String paymentJson);
    void cancelOrder(String orderSn);
}
