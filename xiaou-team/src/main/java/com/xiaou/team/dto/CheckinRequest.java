package com.xiaou.team.dto;

import lombok.Data;

import java.util.List;

/**
 * 打卡请求DTO
 * 
 * @author xiaou
 */
@Data
public class CheckinRequest {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 完成数量
     */
    private Integer completeValue;
    
    /**
     * 打卡内容
     */
    private String content;
    
    /**
     * 图片列表（最多9张）
     */
    private List<String> images;
    
    /**
     * 学习时长（分钟）
     */
    private Integer duration;
    
    /**
     * 相关代码/笔记链接
     */
    private String relatedLink;
}
