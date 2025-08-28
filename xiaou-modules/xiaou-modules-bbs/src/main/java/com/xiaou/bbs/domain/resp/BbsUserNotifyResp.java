package com.xiaou.bbs.domain.resp;

import lombok.Data;

import java.util.Date;

/**
 * 用户BBS论坛通知表
 * @TableName u_bbs_user_notify
 */
@Data
public class BbsUserNotifyResp {

    /**
     * 接收人ID
     */
    private String receiverId;

    /**
     * 通知类型，如like/comment/reply
     */
    private String type;

    /**
     * 目标ID，如被点赞的帖子ID,被评论的帖子ID,被回复的帖子ID
     */
    private String targetId;

    /**
     * 发送人ID（谁点赞的）
     */
    private String senderId;

    /**
     * 是否已读 0=未读 1=已读
     */
    private Integer isRead;

    private Date createdAt;
    /**
     * 新增字段
     */
    private String senderName;
    private String senderAvatar;

    /**
     * 下面字段不一定所有都必填
     */
    private String postTitle;
    /**
     * 评论内容
     */
    private String commentContent;
    /**
     * 回复内容
     */
    private String replyContent;

}