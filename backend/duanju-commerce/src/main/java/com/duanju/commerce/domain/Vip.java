package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_vip")
public class Vip extends BaseEntity {

    private Integer siteId;
    private String title;

    /** 繁体中文名称 */
    private String titleZhTw;

    /** 英文名称 */
    private String titleEn;

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

    /** 返回当前语言对应的 title */
    @JsonGetter("display_title")
    public String getDisplayTitle() {
        Locale locale = LocaleContextHolder.getLocale();
        String lang = locale.toLanguageTag();
        if (lang.startsWith("zh-TW") || lang.startsWith("zh-Hant")) {
            return titleZhTw != null && !titleZhTw.isEmpty() ? titleZhTw : title;
        }
        if (lang.startsWith("en")) {
            return titleEn != null && !titleEn.isEmpty() ? titleEn : title;
        }
        return title;
    }

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
