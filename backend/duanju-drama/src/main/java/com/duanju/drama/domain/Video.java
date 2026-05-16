package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@TableName("vs_drama_video")
public class Video extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer siteId;
    private String title;

    /** 多语言标题，JSON 格式：{"zh-CN":"总裁的秘密","en":"CEO's Secret"} */
    private String titleI18n;

    private String image;
    private String cover;

    /** 总集数 */
    private Integer seriesCount;

    /** 是否连载中: 0=完结 1=连载 */
    private Integer isTv;

    private String score;

    /** 0=隐藏 1=显示 */
    private Integer status;

    /** 分类ID列表，逗号分隔 */
    private String categoryIds;

    private String tags;
    private String description;

    /** 多语言描述，JSON 格式 */
    private String descI18n;

    private String content;
    private String performer;
    private String director;
    private String area;
    private String year;
    private String language;

    /** 真实播放量 */
    private Integer views;

    /** 虚拟播放量（对外显示用） */
    private Integer fakeViews;

    private Integer likes;
    private Integer fakeLikes;
    private Integer collects;
    private Integer shares;
    private Integer comments;

    /** 单集解锁价格（积分） */
    private BigDecimal price;

    /** 是否需要VIP: 0=不需要 1=需要 */
    private Integer isVip;

    /** 免费集数 */
    private Integer freeEpisodes;

    /** 来源类型: local=本地 remote=远程 */
    private String sourceType;

    private String sourceUrl;
    private Integer weigh;
    private String remark;

    @TableField(exist = false)
    private Long totalViews;

    /** 当前用户是否已收藏（非DB字段，接口动态填充） */
    @TableField(exist = false)
    @JsonProperty("is_favorite")
    private Integer isFavorite;

    @JsonGetter("display_title")
    public String getDisplayTitle() {
        return pickI18n(titleI18n, title);
    }

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
