package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户BBS论坛通知表
 * @TableName u_bbs_user_notify
 */
@TableName(value ="u_bbs_user_notify")
@Data
public class BbsUserNotify {
    /**
     *
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;

    /**
     * 接收人ID
     */
    private String receiverId;

    /**
     * 通知类型，如like/comment/reply
     */
    private String type;

    /**
     * 目标ID，如被点赞的帖子ID、评论ID、回复ID
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

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}