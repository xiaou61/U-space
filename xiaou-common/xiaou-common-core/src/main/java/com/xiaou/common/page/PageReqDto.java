package com.xiaou.common.page;

import lombok.Data;

@Data
public class PageReqDto {

    /**
     * 请求页码，默认第 1 页
     */
    private int pageNum = 1;

    /**
     * 每页大小，默认每页 10 条
     */
    private int pageSize = 10;

    private String sortField; // 排序字段

    private Boolean desc = true; // 是否降序，默认 true（倒序）

}