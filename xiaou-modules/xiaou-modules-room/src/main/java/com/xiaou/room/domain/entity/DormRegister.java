package com.xiaou.room.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 宿舍信息登记表
 * @TableName u_dorm_register
 */
@TableName(value ="u_dorm_register")
@Data
public class DormRegister {
    /**
     * 主键ID（UUID或雪花ID）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户学号或唯一账号ID
     */
    private String userId;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;

    /**
     * 身份证号码（支持合法性校验）
     */
    private String idCard;

    /**
     * 登记时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}