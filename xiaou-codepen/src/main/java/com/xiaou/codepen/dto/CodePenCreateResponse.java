package com.xiaou.codepen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 作品创建响应
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodePenCreateResponse {
    
    /**
     * 作品ID
     */
    private Long penId;
    
    /**
     * 奖励积分（首次发布为10）
     */
    private Integer pointsAdded;
    
    /**
     * 当前总积分
     */
    private Integer pointsTotal;
    
    /**
     * 分享链接
     */
    private String shareUrl;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 状态：0-草稿 1-已发布
     */
    private Integer status;
}

