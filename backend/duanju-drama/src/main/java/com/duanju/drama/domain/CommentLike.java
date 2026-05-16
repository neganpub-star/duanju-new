package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_comment_like")
public class CommentLike extends BaseEntity {

    private Long userId;
    private Long commentId;
}
