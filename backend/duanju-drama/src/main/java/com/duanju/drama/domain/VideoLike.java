package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vs_drama_video_like")
public class VideoLike {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer siteId;
    private Long userId;
    private Long videoId;
    private LocalDateTime createTime;
}
