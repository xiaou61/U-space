package com.xiaou.resume.dto;

import lombok.Data;

/**
 * 用户简历列表查询
 */
@Data
public class ResumeListQueryRequest {

    private String keyword;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
