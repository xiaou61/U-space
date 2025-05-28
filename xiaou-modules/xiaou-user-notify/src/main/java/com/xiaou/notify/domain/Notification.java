package com.xiaou.notify.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户消息通知表
 *
 * @TableName u_notification
 */
@TableName(value = "u_notification")
@Data
public class Notification {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 接收通知的用户 ID
     */
    private Long userId;

    /**
     * 触发通知的用户 ID
     */
    private Long fromUserId;

    /**
     * 通知类型（LIKE、COMMENT 等）
     */
    private String type;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联的业务 ID（如帖子 ID）
     */
    private Long bizId;

    private Boolean is_read;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}