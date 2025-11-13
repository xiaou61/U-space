package com.xiaou.codepen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Fork作品响应
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForkResponse {
    
    /**
     * 新作品ID
     */
    private Long newPenId;
    
    /**
     * 原作品ID
     */
    private Long originalPenId;
    
    /**
     * Fork价格
     */
    private Integer forkPrice;
    
    /**
     * 剩余积分
     */
    private Integer pointsRemaining;
    
    /**
     * 作者名称
     */
    private String authorName;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

