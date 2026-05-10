package com.duanju.admin.controller.commerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.domain.UserWalletLog;
import com.duanju.commerce.mapper.UserWalletApplyMapper;
import com.duanju.commerce.mapper.UserWalletLogMapper;
import com.duanju.commerce.service.WalletService;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "【后管】钱包管理")
@RestController
@RequestMapping("/admin/commerce/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final UserWalletApplyMapper walletApplyMapper;
    private final UserWalletLogMapper walletLogMapper;
    private final WalletService walletService;

    @Operation(summary = "提现申请列表")
    @GetMapping("/apply/list")
    public R<PageResult<UserWalletApply>> applyList(PageQuery pageQuery,
                                                     @RequestParam(defaultValue = "1") Integer siteId,
                                                     @RequestParam(required = false) Integer status) {
        Page<UserWalletApply> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        walletApplyMapper.selectPage(page, new LambdaQueryWrapper<UserWalletApply>()
                .eq(UserWalletApply::getSiteId, siteId)
                .eq(status != null, UserWalletApply::getStatus, status)
                .orderByDesc(UserWalletApply::getId));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "审核提现")
    @PutMapping("/apply/{id}")
    public R<Void> handleApply(@PathVariable Long id, @RequestBody HandleReq req) {
        walletService.handleWithdraw(id, req.getStatus(), req.getRemark());
        return R.ok();
    }

    @Operation(summary = "钱包流水")
    @GetMapping("/logs")
    public R<PageResult<UserWalletLog>> logs(PageQuery pageQuery,
                                              @RequestParam(defaultValue = "1") Integer siteId,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) String walletType) {
        Page<UserWalletLog> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        walletLogMapper.selectPage(page, new LambdaQueryWrapper<UserWalletLog>()
                .eq(UserWalletLog::getSiteId, siteId)
                .eq(userId != null, UserWalletLog::getUserId, userId)
                .eq(walletType != null, UserWalletLog::getWalletType, walletType)
                .orderByDesc(UserWalletLog::getId));
        return R.ok(PageResult.of(page));
    }

    @Data
    static class HandleReq {
        private Integer status;
        private String remark;
    }
}
