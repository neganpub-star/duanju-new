package com.duanju.drama.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_watch_log")
public class WatchLog extends BaseEntity {

    private Integer siteId;
    private Long userId;
    private Long videoId;
    private Long episodeId;
    private BigDecimal viewTime;
}
