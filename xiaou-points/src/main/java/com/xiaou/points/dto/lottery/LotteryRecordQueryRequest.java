package com.xiaou.points.dto.lottery;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽奖记录查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class LotteryRecordQueryRequest {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 20;
    
    /**
     * 用户ID（内部使用）
     */
    private Long userId;
    
    public Integer getPage() {
        return pageNum;
    }
    
    public Integer getSize() {
        return pageSize;
    }
    
    /**
     * 奖品等级筛选
     */
    private Integer prizeLevel;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}

