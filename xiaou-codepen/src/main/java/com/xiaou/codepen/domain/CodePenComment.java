package com.xiaou.codepen.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作品评论实体
 * 
 * @author xiaou
 */
@Data
public class CodePenComment {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 作品ID
     */
    private Long penId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 评论用户昵称（非数据库字段，查询时填充）
     */
    private transient String userNickname;
    
    /**
     * 评论用户头像（非数据库字段，查询时填充）
     */
    private transient String userAvatar;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID（回复）
     */
    private Long parentId;
    
    /**
     * 回复目标用户ID
     */
    private Long replyToUserId;
    
    /**
     * 状态：1-正常 2-已隐藏 3-已删除
     */
    private Integer status;
    
    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}

