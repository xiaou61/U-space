package com.xiaou.codepen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查Fork价格响应
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckForkPriceResponse {
    
    /**
     * 作品ID
     */
    private Long penId;
    
    /**
     * 是否免费
     */
    private Boolean isFree;
    
    /**
     * Fork价格
     */
    private Integer forkPrice;
    
    /**
     * 当前用户积分
     */
    private Integer currentPoints;
    
    /**
     * 是否可以Fork
     */
    private Boolean canFork;
    
    /**
     * 作者名称
     */
    private String authorName;
    
    /**
     * 提示信息
     */
    private String message;
}

