package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_comment")
public class Comment extends BaseEntity {

    private Integer siteId;
    private Long userId;
    private Long videoId;
    private Long parentId;
    private Long replyUserId;
    private String replyNickname;
    private String content;
    private Integer likes;
    private Integer status;
}
