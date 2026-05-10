package com.duanju.commerce.service;

import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.domain.UserWalletLog;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    void addWallet(Integer siteId, Long userId, String walletType, BigDecimal amount, String changeType, String memo, String itemId);
    void deductWallet(Integer siteId, Long userId, String walletType, BigDecimal amount, String changeType, String memo, String itemId);
    List<UserWalletLog> getLogs(Integer siteId, Long userId, String walletType, int page, int size);
    UserWalletApply applyWithdraw(Integer siteId, Long userId, String applyType, BigDecimal money, String applyInfo, String platform);
    void handleWithdraw(Long applyId, Integer status, String remark);
}
