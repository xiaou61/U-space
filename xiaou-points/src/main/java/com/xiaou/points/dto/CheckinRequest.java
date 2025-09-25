package com.xiaou.points.dto;

import lombok.Data;

/**
 * 用户打卡请求DTO
 * 
 * @author xiaou
 */
@Data
public class CheckinRequest {
    
    /**
     * 用户ID（从Token中获取，前端无需传递）
     */
    private Long userId;
}
