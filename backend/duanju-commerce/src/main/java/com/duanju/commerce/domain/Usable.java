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
@TableName("vs_drama_usable")
public class Usable extends BaseEntity {

    private Integer siteId;
    private String title;

    /** 繁体中文名称 */
    private String titleZhTw;

    /** 英文名称 */
    private String titleEn;

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
}
