package com.duanju.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.domain.UserWalletLog;
import com.duanju.commerce.mapper.UserWalletApplyMapper;
import com.duanju.commerce.service.WalletService;
import com.duanju.common.core.domain.R;
import com.duanju.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "钱包接口")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserWalletApplyMapper walletApplyMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "钱包流水")
    @GetMapping("/logs")
    public R<List<UserWalletLog>> logs(
            @RequestParam(required = false) String walletType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(walletService.getLogs(siteId, SecurityUtil.getUserId(), walletType, page, size));
    }

    @Operation(summary = "提现记录")
    @GetMapping("/withdraw-list")
    public R<Map<String, Object>> withdrawList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pagesize) {
        long userId = SecurityUtil.getUserId();
        Page<UserWalletApply> p = walletApplyMapper.selectPage(
                new Page<>(page, pagesize),
                new LambdaQueryWrapper<UserWalletApply>()
                        .eq(UserWalletApply::getUserId, userId)
                        .orderByDesc(UserWalletApply::getCreateTime));
        Map<String, Object> result = new HashMap<>();
        result.put("data", p.getRecords().stream().map(a -> {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("money", a.getMoney());
            item.put("apply_type", a.getApplyType());
            item.put("apply_type_text", switch (a.getApplyType() == null ? "" : a.getApplyType()) {
                case "wechat" -> "微信";
                case "alipay" -> "支付宝";
                case "bank" -> "银行卡";
                default -> a.getApplyType();
            });
            item.put("status", a.getStatus());
            item.put("status_text", switch (a.getStatus()) {
                case -1 -> "已拒绝";
                case 0 -> "待审核";
                case 1 -> "处理中";
                case 2 -> "已完成";
                default -> "未知";
            });
            item.put("createtime", a.getCreateTime() != null ? a.getCreateTime().toEpochSecond(java.time.ZoneOffset.ofHours(8)) : 0);
            return item;
        }).toList());
        result.put("total", p.getTotal());
        return R.ok(result);
    }

    @Operation(summary = "申请提现")
    @PostMapping("/withdraw")
    public R<UserWalletApply> withdraw(@Valid @RequestBody WithdrawReq req) {
        return R.ok(walletService.applyWithdraw(siteId, SecurityUtil.getUserId(),
                req.getApplyType(), req.getMoney(), req.getApplyInfo(), req.getPlatform()));
    }

    @Data
    static class WithdrawReq {
        @NotBlank(message = "error.withdraw.type.required")
        private String applyType;
        @DecimalMin(value = "0.01", message = "error.withdraw.amount.min")
        private BigDecimal money;
        private String applyInfo;
        private String platform;
    }
}
