package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_richtext")
public class RichText extends BaseEntity {

    private String title;

    private String lang;

    private String docKey;

    private String content;
}
