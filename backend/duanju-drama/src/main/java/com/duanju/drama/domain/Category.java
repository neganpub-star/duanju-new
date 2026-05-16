package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_category")
public class Category extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer siteId;
    private Long pid;
    private String name;

    /** 多语言名称，JSON 格式：{"zh-CN":"都市爱情","en":"Urban Romance"} */
    private String nameI18n;

    /** 类型: video=视频 year=年份 area=地区 */
    private String type;

    /** 层级: 1=一级 2=二级 3=三级 */
    private Integer style;

    private String image;
    private Integer weigh;
    private String description;

    /** normal / hidden */
    private String status;

    @TableField(exist = false)
    private List<Category> children;

    @JsonGetter("display_name")
    public String getDisplayName() {
        return pickI18n(nameI18n, name);
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
