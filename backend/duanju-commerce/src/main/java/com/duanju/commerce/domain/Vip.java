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
@TableName("vs_drama_vip")
public class Vip extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer siteId;
    private String title;

    /** 多语言名称，JSON 格式：{"zh-CN":"月卡VIP","zh-TW":"月卡VIP","en":"Monthly VIP"} */
    private String titleI18n;

    private String image;
    private String description;

    /** 多语言描述，JSON 格式 */
    private String descI18n;

    private String content;

    /** 有效天数 */
    private Integer days;

    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer weigh;

    /** normal / hidden */
    private String status;

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

    private static String pickI18n(String json, String fallback) {
        if (json == null || json.isBlank()) return fallback;
        try {
            String lang = LocaleContextHolder.getLocale().toLanguageTag();
            @SuppressWarnings("unchecked")
            Map<String, String> map = MAPPER.readValue(json, Map.class);
            // 精确匹配
            if (map.containsKey(lang)) return map.get(lang);
            // 语言前缀匹配（zh-TW 匹配 zh-Hant 等）
            String prefix = lang.split("-")[0];
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (e.getKey().startsWith(prefix)) return e.getValue();
            }
            // 回退：zh-CN > 第一个值 > fallback
            return map.getOrDefault("zh-CN", map.values().stream().findFirst().orElse(fallback));
        } catch (Exception e) {
            log.warn("Failed to parse i18n JSON: {}", json);
            return fallback;
        }
    }
}
