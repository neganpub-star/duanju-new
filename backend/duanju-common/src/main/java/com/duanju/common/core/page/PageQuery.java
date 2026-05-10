package com.duanju.common.core.page;

import lombok.Data;

@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderByColumn;
    private String isAsc = "desc";

    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}
