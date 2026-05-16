package com.duanju.commerce.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.domain.UserWalletLog;
import com.duanju.commerce.mapper.UserWalletApplyMapper;
import com.duanju.commerce.mapper.UserWalletLogMapper;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final DramaUserMapper userMapper;
    private final UserWalletLogMapper walletLogMapper;
    private final UserWalletApplyMapper walletApplyMapper;

    @Autowired(required = false)
    private MessageSource messageSource;

    private String msg(String key, Object... args) {
        if (messageSource == null) return key;
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWallet(Integer siteId, Long userId, String walletType, BigDecimal amount, String changeType, String memo, String itemId) {
        changeWallet(siteId, userId, walletType, amount, changeType, memo, itemId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductWallet(Integer siteId, Long userId, String walletType, BigDecimal amount, String changeType, String memo, String itemId) {
        changeWallet(siteId, userId, walletType, amount.negate(), changeType, memo, itemId, true);
    }

    private void changeWallet(Integer siteId, Long userId, String walletType, BigDecimal amount,
                               String changeType, String memo, String itemId, boolean checkBalance) {
        DramaUser user = userMapper.selectById(userId);
        if (user == null) throw ServiceException.notFound(msg("error.user.notfound"));

        BigDecimal before = switch (walletType) {
            case "money" -> user.getMoney();
            case "score" -> user.getScore();
            case "usable" -> user.getUsable();
            default -> throw ServiceException.of(msg("error.wallet.type"));
        };
        BigDecimal after = before.add(amount);
        if (checkBalance && after.compareTo(BigDecimal.ZERO) < 0) {
            throw ServiceException.of(msg("error.insufficient.balance"));
        }

        DramaUser update = new DramaUser();
        update.setId(userId);
        switch (walletType) {
            case "money" -> update.setMoney(after);
            case "score" -> update.setScore(after);
            case "usable" -> update.setUsable(after);
        }
        userMapper.updateById(update);

        UserWalletLog log = new UserWalletLog();
        log.setSiteId(siteId);
        log.setUserId(userId);
        log.setWallet(amount);
        log.setWalletType(walletType);
        log.setType(changeType);
        log.setBefore(before);
        log.setAfter(after);
        log.setItemId(itemId);
        log.setMemo(memo);
        walletLogMapper.insert(log);
    }

    @Override
    public List<UserWalletLog> getLogs(Integer siteId, Long userId, String walletType, int page, int size) {
        Page<UserWalletLog> p = new Page<>(page, size);
        LambdaQueryWrapper<UserWalletLog> wrapper = new LambdaQueryWrapper<UserWalletLog>()
                .eq(UserWalletLog::getUserId, userId)
                .eq(walletType != null, UserWalletLog::getWalletType, walletType)
                .orderByDesc(UserWalletLog::getId);
        return walletLogMapper.selectPage(p, wrapper).getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserWalletApply applyWithdraw(Integer siteId, Long userId, String applyType,
                                          BigDecimal money, String applyInfo, String platform) {
        DramaUser user = userMapper.selectById(userId);
        if (user == null) throw ServiceException.notFound(msg("error.user.notfound"));
        if (user.getMoney().compareTo(money) < 0) throw ServiceException.of(msg("error.wallet.balance"));

        deductWallet(siteId, userId, "money", money, "withdraw", "申请提现", null);

        UserWalletApply apply = new UserWalletApply();
        apply.setSiteId(siteId);
        apply.setUserId(userId);
        apply.setApplySn("WD" + IdUtil.getSnowflakeNextIdStr());
        apply.setApplyType(applyType);
        apply.setMoney(money);
        apply.setChargeMoney(BigDecimal.ZERO);
        apply.setActualMoney(money);
        apply.setApplyInfo(applyInfo);
        apply.setStatus(0);
        apply.setPlatform(platform);
        walletApplyMapper.insert(apply);
        return apply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWithdraw(Long applyId, Integer status, String remark) {
        UserWalletApply apply = walletApplyMapper.selectById(applyId);
        if (apply == null) throw ServiceException.notFound(msg("error.wallet.not.found"));
        if (apply.getStatus() != 0 && apply.getStatus() != 1) throw ServiceException.of(msg("error.wallet.state"));

        // 拒绝时退款
        if (status == -1) {
            addWallet(apply.getSiteId(), apply.getUserId(), "money", apply.getMoney(), "withdraw_reject", "提现拒绝退款", apply.getApplySn());
        }

        apply.setStatus(status);
        apply.setLog(remark);
        walletApplyMapper.updateById(apply);
    }
}
