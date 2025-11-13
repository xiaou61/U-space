package com.xiaou.codepen.dto;

import lombok.Data;

/**
 * 更新状态请求
 * 
 * @author xiaou
 */
@Data
public class UpdateStatusRequest {
    
    /**
     * 作品ID
     */
    private Long id;
    
    /**
     * 状态：0-草稿 1-已发布 2-已下架 3-已删除
     */
    private Integer status;
}

