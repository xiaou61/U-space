package com.xiaou.codepen.dto;

import lombok.Data;

/**
 * Fork作品请求
 * 
 * @author xiaou
 */
@Data
public class ForkRequest {
    
    /**
     * 作品ID
     */
    private Long penId;
}

