package com.duanju.commerce.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * VIP 订单全量统计
 */
@Data
public class VipOrderStatsVO {

    /** 订单总数 */
    private Long totalCount;

    /** 已支付订单数（status=1 或 2） */
    private Long paidCount;

    /** 待支付订单数（status=0） */
    private Long pendingCount;

    /** 已取消/已关闭订单数（status=-1 或 -2） */
    private Long canceledCount;

    /** 累计 GMV（已支付订单 payFee 总和） */
    private BigDecimal gmv;
}
