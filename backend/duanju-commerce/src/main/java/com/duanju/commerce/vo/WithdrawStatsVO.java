package com.duanju.commerce.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现申请状态分布统计
 */
@Data
public class WithdrawStatsVO {

    /** 待审核数量（status=0） */
    private Long pendingCount;

    /** 处理中数量（status=1） */
    private Long processingCount;

    /** 已完成数量（status=2） */
    private Long doneCount;

    /** 已拒绝数量（status=-1） */
    private Long rejectedCount;

    /** 累计已打款金额（status=1 的 money 之和） */
    private BigDecimal totalDoneAmount;
}
