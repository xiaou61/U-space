package com.xiaou.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题单列表查询请求
 *
 * @author xiaou
 */
@Data
@Schema(description = "题单列表查询请求")
public class QuestionSetListRequest {
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;
    
    @Schema(description = "每页数量", defaultValue = "10")
    private Integer size = 10;
}
