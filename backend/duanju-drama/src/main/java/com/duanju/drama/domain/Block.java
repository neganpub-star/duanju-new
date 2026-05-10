package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_block")
public class Block extends BaseEntity {

    private Integer siteId;

    /** focus=焦点图 side=广告图 */
    private String type;

    private String name;
    private String title;
    private String image;
    private String url;

    /** 0=外部链接 1=内部链接 */
    private Integer parsetpl;

    private Integer weigh;

    /** normal / hidden */
    private String status;
}
