package com.xiaou.sse.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

/**
 * 用户通知消息表
 * @TableName u_user_notify_message
 */
@TableName(value ="u_user_notify_message")
@Data
public class UserNotifyMessage {
    /**
     * 消息ID，UUID主键（无连字符）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 接收者用户ID，为All表示广播公告
     */
    private String userId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（如：system、like、comment）
     */
    private String type;


    /**
     * 是否通过了SSE推送：0否，1是
     */
    private Integer isPush;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}