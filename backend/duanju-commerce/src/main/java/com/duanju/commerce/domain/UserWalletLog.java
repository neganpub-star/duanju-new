package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_user_wallet_log")
public class UserWalletLog extends BaseEntity {

    private Integer siteId;
    private Long userId;

    /** 变动金额（正=增加 负=减少） */
    private BigDecimal wallet;

    /** money / score / usable */
    private String walletType;

    /** 变动类型标识 */
    private String type;

    private BigDecimal before;
    private BigDecimal after;

    /** 关联业务ID */
    private String itemId;

    private String memo;
}
