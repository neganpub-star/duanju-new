package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_category")
public class Category extends BaseEntity {

    private Integer siteId;
    private Long pid;
    private String name;

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
}
