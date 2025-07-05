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
 * 签到记录表
 * @TableName u_signin_record
 */
@TableName(value ="u_signin_record")
@Data
public class SigninRecord {
    /**
     * 签到记录ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 签到任务ID，关联u_signin表
     */
    private String signinId;

    /**
     * 签到人ID，关联u_student表
     */
    private String studentId;

    private Integer type;

    /**
     * 签到时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date signinTime;

    /**
     * 是否迟到：0-未迟到，1-迟到
     */
    private Integer isLate;

    /**
     * 密码，仅密码签到时使用
     */
    private String password;

    /**
     * 签到纬度，仅位置签到时使用
     */
    private BigDecimal latitude;

    /**
     * 签到经度，仅位置签到时使用
     */
    private BigDecimal longitude;

    /**
     * 签到状态：0-无效，1-有效
     */
    private Integer status;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 记录更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}