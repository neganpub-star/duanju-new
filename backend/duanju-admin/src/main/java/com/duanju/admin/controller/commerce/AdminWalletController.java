package com.duanju.admin.controller.commerce;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.mapper.ResellerOrderMapper;
import com.duanju.commerce.mapper.UsableOrderMapper;
import com.duanju.commerce.mapper.UserWalletApplyMapper;
import com.duanju.commerce.mapper.UserWalletLogMapper;
import com.duanju.commerce.mapper.VipOrderMapper;
import com.duanju.commerce.service.WalletService;
import com.duanju.commerce.vo.WalletLogVO;
import com.duanju.commerce.vo.WithdrawStatsVO;
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
    private final VipOrderMapper vipOrderMapper;
    private final UsableOrderMapper usableOrderMapper;
    private final ResellerOrderMapper resellerOrderMapper;
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

    @Operation(summary = "提现申请状态分布统计")
    @GetMapping("/apply/stats")
    public R<WithdrawStatsVO> applyStats(@RequestParam(defaultValue = "1") Integer siteId) {
        return R.ok(walletApplyMapper.selectApplyStats(siteId));
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

    @Operation(summary = "资金流水统计（日/周/月/年）",
               description = "三类金额（单位均为元）：\n" +
                       "- *Revenue 业务订单收入：VIP+点数+分销三类订单的 payFee 之和（status=1或2）\n" +
                       "- *Income 余额账户入账：vs_drama_user_wallet_log 中 wallet_type='money' 且 wallet>0 的流水之和\n" +
                       "- *Withdraw 实际打款提现：vs_drama_user_wallet_apply 中 status=1（已通过）的金额之和")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats(@RequestParam(defaultValue = "1") Integer siteId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart  = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime yearStart  = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();

        Map<String, Object> result = new LinkedHashMap<>();
        // 业务订单收入（VIP + 点数 + 分销）
        result.put("todayRevenue", orderRevenue(siteId, todayStart));
        result.put("weekRevenue",  orderRevenue(siteId, weekStart));
        result.put("monthRevenue", orderRevenue(siteId, monthStart));
        result.put("yearRevenue",  orderRevenue(siteId, yearStart));
        // 余额账户入账
        result.put("todayIncome", walletLogMapper.sumIncomeFrom(siteId, todayStart));
        result.put("weekIncome",  walletLogMapper.sumIncomeFrom(siteId, weekStart));
        result.put("monthIncome", walletLogMapper.sumIncomeFrom(siteId, monthStart));
        result.put("yearIncome",  walletLogMapper.sumIncomeFrom(siteId, yearStart));
        // 已打款提现
        result.put("todayWithdraw", walletLogMapper.sumWithdrawFrom(siteId, todayStart));
        result.put("weekWithdraw",  walletLogMapper.sumWithdrawFrom(siteId, weekStart));
        result.put("monthWithdraw", walletLogMapper.sumWithdrawFrom(siteId, monthStart));
        result.put("yearWithdraw",  walletLogMapper.sumWithdrawFrom(siteId, yearStart));
        return R.ok(result);
    }

    /** 三类订单已支付金额聚合 */
    private BigDecimal orderRevenue(Integer siteId, LocalDateTime start) {
        BigDecimal vip      = nullToZero(vipOrderMapper.sumPayFeeFrom(siteId, start));
        BigDecimal usable   = nullToZero(usableOrderMapper.sumPayFeeFrom(siteId, start));
        BigDecimal reseller = nullToZero(resellerOrderMapper.sumPayFeeFrom(siteId, start));
        return vip.add(usable).add(reseller);
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Data
    static class HandleReq {
        private Integer status;
        private String remark;
    }
}
