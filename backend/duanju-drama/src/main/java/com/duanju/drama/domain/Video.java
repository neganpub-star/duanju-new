package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_video")
public class Video extends BaseEntity {

    private Integer siteId;
    private String title;
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
}
