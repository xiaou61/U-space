package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组申请实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamApplication {
    
    /**
     * 申请ID
     */
    private Long id;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 申请人ID
     */
    private Long userId;
    
    /**
     * 申请理由
     */
    private String applyReason;
    
    /**
     * 状态：0待审核 1已通过 2已拒绝 3已取消
     */
    private Integer status;
    
    /**
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
