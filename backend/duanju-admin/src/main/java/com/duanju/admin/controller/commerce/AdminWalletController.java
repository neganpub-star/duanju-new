package com.duanju.admin.controller.commerce;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.mapper.UserWalletApplyMapper;
import com.duanju.commerce.mapper.UserWalletLogMapper;
import com.duanju.commerce.service.WalletService;
import com.duanju.commerce.vo.WalletLogVO;
import com.duanju.commerce.vo.WithdrawVO;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "【后管】钱包管理")
@RestController
@RequestMapping("/admin/commerce/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final UserWalletApplyMapper walletApplyMapper;
    private final UserWalletLogMapper walletLogMapper;
    private final WalletService walletService;

    @Operation(summary = "提现申请列表（含用户信息）")
    @GetMapping("/apply/list")
    public R<PageResult<WithdrawVO>> applyList(PageQuery pageQuery,
                                               @RequestParam(defaultValue = "1") Integer siteId,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) String mobile,
                                               @RequestParam(required = false) String nickname) {
        Page<WithdrawVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        String mob = StringUtils.hasText(mobile) ? mobile : null;
        String nick = StringUtils.hasText(nickname) ? nickname : null;
        walletApplyMapper.selectWithUserPage(page, siteId, status, mob, nick);
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "审核提现")
    @PutMapping("/apply/{id}")
    public R<Void> handleApply(@PathVariable Long id, @RequestBody HandleReq req) {
        walletService.handleWithdraw(id, req.getStatus(), req.getRemark());
        return R.ok();
    }

    @Operation(summary = "钱包流水（含用户信息）")
    @GetMapping("/logs")
    public R<PageResult<WalletLogVO>> logs(PageQuery pageQuery,
                                            @RequestParam(defaultValue = "1") Integer siteId,
                                            @RequestParam(required = false) String walletType,
                                            @RequestParam(required = false) String mobile,
                                            @RequestParam(required = false) String nickname) {
        Page<WalletLogVO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        String wt  = StringUtils.hasText(walletType) ? walletType : null;
        String mob = StringUtils.hasText(mobile)     ? mobile     : null;
        String nik = StringUtils.hasText(nickname)   ? nickname   : null;
        walletLogMapper.selectWithUserPage(page, siteId, wt, mob, nik);
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "入金/出金统计（日/周/月/年）")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats(@RequestParam(defaultValue = "1") Integer siteId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime yearStart = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayIncome", walletLogMapper.sumIncomeFrom(siteId, todayStart));
        result.put("weekIncome", walletLogMapper.sumIncomeFrom(siteId, weekStart));
        result.put("monthIncome", walletLogMapper.sumIncomeFrom(siteId, monthStart));
        result.put("yearIncome", walletLogMapper.sumIncomeFrom(siteId, yearStart));
        result.put("todayWithdraw", walletLogMapper.sumWithdrawFrom(siteId, todayStart));
        result.put("weekWithdraw", walletLogMapper.sumWithdrawFrom(siteId, weekStart));
        result.put("monthWithdraw", walletLogMapper.sumWithdrawFrom(siteId, monthStart));
        result.put("yearWithdraw", walletLogMapper.sumWithdrawFrom(siteId, yearStart));
        return R.ok(result);
    }

    @Data
    static class HandleReq {
        private Integer status;
        private String remark;
    }
}
