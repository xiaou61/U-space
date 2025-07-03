package com.xiaou.system.bulletin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 系统公告表
 * @TableName sys_announcement
 */
@TableName(value ="sys_announcement")
@Data
public class Announcement {
    /**
     * 公告ID，UUID主键（无连字符）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告发布时间（默认当前时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    /**
     * 是否需要弹框提醒（0否，1是）
     */
    private Integer needPopup;

    /**
     * 是否逻辑删除（0否，1是）
     */
    @TableLogic
    private Integer isDeleted;
}