package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRawValue;
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
@TableName("vs_drama_video_episodes")
public class VideoEpisodes extends BaseEntity {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer siteId;
    private Long videoId;
    private String title;

    /** 多语言标题，JSON 格式：{"zh-CN":"第1集","zh-TW":"第1集","en":"Episode 1"} */
    private String titleI18n;

    /** 时长（秒） */
    private Integer duration;

    /** 视频播放地址 */
    private String url;

    /** HLS 地址 */
    private String hlsUrl;

    /** 第几集 */
    private Integer episodeNum;

    /** 0=收费 1=免费 */
    private Integer isFree;

    /** 解锁价格（积分），0=跟随主剧设置 */
    private BigDecimal price;

    private Integer views;
    private Integer weigh;

    /** 0=隐藏 1=显示 */
    private Integer status;

    /** 多清晰度播放信息，JSON数组：[{definition,url},...] */
    @JsonRawValue
    private String playInfo;

    /** 转码状态: pending / processing / done / failed */
    private String transcodeStatus;

    /** 转码失败原因 */
    private String transcodeMsg;

    @JsonGetter("display_title")
    public String getDisplayTitle() {
        return pickI18n(titleI18n, title);
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
