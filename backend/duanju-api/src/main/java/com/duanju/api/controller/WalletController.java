package com.duanju.api.controller;

import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.domain.UserWalletLog;
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
import java.util.List;

@Tag(name = "钱包接口")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

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
