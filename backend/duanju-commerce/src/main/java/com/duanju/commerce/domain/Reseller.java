package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_reseller")
public class Reseller extends BaseEntity {

    private Integer siteId;
    private String name;
    /** 多语言名称 JSON，如 {"zh-CN":"普通分销商","en":"Regular Reseller"} */
    private String nameI18n;
    private String image;
    private String content;

    private BigDecimal price;
    private BigDecimal originalPrice;

    /** 分销等级 */
    private Integer level;

    /** 直接分润比例（%） */
    private BigDecimal direct;

    /** 间接分润比例（%） */
    private BigDecimal indirect;

    /** 有效天数 */
    private Integer expire;

    private Integer weigh;

    /** normal / hidden */
    private String status;
}
