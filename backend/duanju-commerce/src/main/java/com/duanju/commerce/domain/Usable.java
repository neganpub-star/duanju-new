package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_usable")
public class Usable extends BaseEntity {

    private Integer siteId;
    private String title;
    private String image;
    private String flag;
    private String description;
    private String content;

    /** 赠送总点数 */
    private Integer usable;
    private Integer originalUsable;
    private Integer giveUsable;

    private BigDecimal price;
    private BigDecimal givePrice;
    private BigDecimal firstPrice;
    private BigDecimal originalPrice;

    /** 0=不启用 1=启用 */
    private String status;

    private Integer weigh;
}
