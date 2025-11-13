package com.xiaou.codepen.dto;

import lombok.Data;

/**
 * 检查Fork价格请求
 * 
 * @author xiaou
 */
@Data
public class CheckForkPriceRequest {
    
    /**
     * 作品ID
     */
    private Long penId;
}

