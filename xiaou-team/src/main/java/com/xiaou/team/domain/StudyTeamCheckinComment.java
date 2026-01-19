package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡评论实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamCheckinComment {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 打卡ID
     */
    private Long checkinId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 父评论ID
     */
    private Long parentId;
    
    /**
     * 回复用户ID
     */
    private Long replyUserId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态：0已删除 1正常
     */
    private Integer status;
    
    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
