package com.xiaou.codepen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码共享器统计数据响应
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodePenStatisticsResponse {
    
    /**
     * 作品总数
     */
    private Long totalPens;
    
    /**
     * 用户总数
     */
    private Long totalUsers;
    
    /**
     * 今日新增
     */
    private Long todayNewPens;
    
    /**
     * 总浏览量
     */
    private Long totalViews;
    
    /**
     * 总点赞数
     */
    private Long totalLikes;
    
    /**
     * 总评论数
     */
    private Long totalComments;
    
    /**
     * 总收藏数
     */
    private Long totalCollects;
    
    /**
     * 总Fork数
     */
    private Long totalForks;
}

