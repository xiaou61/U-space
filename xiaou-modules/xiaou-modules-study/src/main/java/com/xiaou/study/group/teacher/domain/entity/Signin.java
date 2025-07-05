package com.xiaou.study.group.teacher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 群组签到任务表
 * @TableName u_signin
 */
@TableName(value ="u_signin")
@Data
public class Signin {
    /**
     * 签到任务ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 群组ID，关联u_group表
     */
    private String groupId;

    /**
     * 发起人ID（教师ID）
     */
    private String creatorId;

    /**
     * 签到类型：0=普通，1=密码，2=位置，3=拍照
     */
    private Integer type;

    /**
     * 签到密码（仅限密码签到）
     */
    private String password;

    /**
     * 纬度（位置签到用）
     */
    private BigDecimal latitude;

    /**
     * 经度（位置签到用）
     */
    private BigDecimal longitude;

    /**
     * 允许的签到范围（单位：米）
     */
    private Integer locationRadius;

    /**
     * 签到截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}