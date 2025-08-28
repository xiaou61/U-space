package com.xiaou.bbs.mq;

import lombok.Data;

@Data
public class BbsUserNotifyMq {

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
     * 发送人ID
     */
    private String senderId;
}
