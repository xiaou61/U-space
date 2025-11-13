package com.xiaou.resume.dto;

import lombok.Data;

import java.util.List;

/**
 * 模板查询请求
 */
@Data
public class TemplateQueryRequest {

    private String keyword;
    private String category;
    private Integer experienceLevel;
    private List<String> tags;
    private Integer status;
    private Integer page = 1;
    private Integer size = 10;
}
