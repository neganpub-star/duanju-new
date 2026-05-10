package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_vip")
public class Vip extends BaseEntity {

    private Integer siteId;
    private String title;
    private String image;
    private String description;
    private String content;

    /** 有效天数 */
    private Integer days;

    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer weigh;

    /** normal / hidden */
    private String status;
}
