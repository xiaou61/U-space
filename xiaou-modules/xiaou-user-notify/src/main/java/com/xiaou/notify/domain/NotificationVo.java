package com.xiaou.notify.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;


@Data
@AutoMapper(target = Notification.class)
public class NotificationVo {
    /**
     * 主键
     */
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

    /**
     * 创建时间
     */
    private Date createTime;

    private String fromUserName;
    private String fromUserAvatar;
}