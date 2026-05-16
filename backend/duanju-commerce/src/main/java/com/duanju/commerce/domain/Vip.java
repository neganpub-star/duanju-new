package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
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

    /** 前端展示用：根据天数生成文字 */
    @JsonGetter("type_text")
    public String getTypeText() {
        if (days == null) return "";
        if (days <= 3)  return "体验";
        if (days <= 7)  return "周";
        if (days <= 31) return "月";
        if (days <= 93) return "季";
        return "年";
    }
}
