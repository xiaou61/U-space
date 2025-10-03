package com.xiaou.moment.dto;

import lombok.Data;

/**
 * 热门动态请求DTO
 */
@Data
public class HotMomentRequest {
    
    /**
     * 获取数量，默认3条
     */
    private Integer limit = 3;
}

