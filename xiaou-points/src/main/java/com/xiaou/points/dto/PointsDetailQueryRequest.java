package com.xiaou.points.dto;

import lombok.Data;

/**
 * 积分明细查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class PointsDetailQueryRequest {
    
    /**
     * 用户ID（用户端查询时必填）
     */
    private Long userId;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 积分类型筛选：1-后台发放 2-打卡积分
     */
    private Integer pointsType;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 管理员ID（管理端查询时使用）
     */
    private Long adminId;
    
    /**
     * 用户名模糊搜索（管理端使用）
     */
    private String userName;
}
