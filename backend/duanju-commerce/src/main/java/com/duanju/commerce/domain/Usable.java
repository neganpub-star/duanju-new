package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_usable")
public class Usable extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer siteId;
    private String title;

    /** 多语言名称，JSON 格式：{"zh-CN":"100点数","en":"100 Points"} */
    private String titleI18n;

    private String image;
    private String flag;
    private String description;

    /** 多语言描述，JSON 格式 */
    private String descI18n;

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

    /** 按当前请求语言返回名称，回退到 title */
    @JsonGetter("display_title")
    public String getDisplayTitle() {
        return pickI18n(titleI18n, title);
    }

    /** 按当前请求语言返回描述，回退到 description */
    @JsonGetter("display_desc")
    public String getDisplayDesc() {
        return pickI18n(descI18n, description);
    }

    private static String pickI18n(String json, String fallback) {
        if (json == null || json.isBlank()) return fallback;
        try {
            String lang = LocaleContextHolder.getLocale().toLanguageTag();
            @SuppressWarnings("unchecked")
            Map<String, String> map = MAPPER.readValue(json, Map.class);
            if (map.containsKey(lang)) return map.get(lang);
            String prefix = lang.split("-")[0];
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (e.getKey().startsWith(prefix)) return e.getValue();
            }
            return map.getOrDefault("zh-CN", map.values().stream().findFirst().orElse(fallback));
        } catch (Exception e) {
            log.warn("Failed to parse i18n JSON: {}", json);
            return fallback;
        }
    }
}
