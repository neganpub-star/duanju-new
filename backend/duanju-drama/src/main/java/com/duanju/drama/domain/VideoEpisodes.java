package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_video_episodes")
public class VideoEpisodes extends BaseEntity {

    private Integer siteId;
    private Long videoId;
    private String title;

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
}
