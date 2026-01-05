package com.xiaou.team.dto;

import lombok.Data;

/**
 * 加入小组请求DTO
 * 
 * @author xiaou
 */
@Data
public class JoinRequest {
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 申请理由
     */
    private String applyReason;
    
    /**
     * 邀请码（通过邀请码加入时使用）
     */
    private String inviteCode;
}
