package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_user_wallet_apply")
public class UserWalletApply extends BaseEntity {

    private Integer siteId;
    private Long userId;
    private String applySn;

    /** bank / wechat / alipay */
    private String applyType;

    private BigDecimal money;
    private BigDecimal actualMoney;
    private BigDecimal chargeMoney;
    private BigDecimal serviceFee;

    /** 收款账户信息 JSON */
    private String applyInfo;

    /** -1=已拒绝 0=待审核 1=处理中 2=已处理 */
    private Integer status;

    private String platform;
    private String paymentJson;
    private String log;
}
