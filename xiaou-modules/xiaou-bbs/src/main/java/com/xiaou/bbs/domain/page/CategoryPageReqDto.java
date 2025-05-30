package com.xiaou.bbs.domain.page;

import lombok.Data;

@Data
public class CategoryPageReqDto {
    private int pageNum;
    private int pageSize;
    private String sortField;
    private String category;
    private Boolean desc = true;
}
