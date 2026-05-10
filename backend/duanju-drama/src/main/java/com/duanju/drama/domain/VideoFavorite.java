package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_video_favorite")
public class VideoFavorite extends BaseEntity {

    private Integer siteId;
    private Long userId;
    private Long videoId;

    @TableField(exist = false)
    private Video video;
}
