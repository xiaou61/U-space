package com.xiaou.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 博客开通状态检查响应DTO
 * 
 * @author xiaou
 */
@Data
public class BlogCheckStatusResponse {
    
    /**
     * 是否已开通
     */
    private Boolean isOpened;
    
    /**
     * 博客ID（已开通时）
     */
    private Long blogId;
    
    /**
     * 当前积分（未开通时）
     */
    private Integer currentPoints;
    
    /**
     * 需要积分（未开通时）
     */
    private Integer requiredPoints;
    
    /**
     * 是否可开通（未开通时）
     */
    private Boolean canOpen;
    
    /**
     * 创建时间（已开通时）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}


