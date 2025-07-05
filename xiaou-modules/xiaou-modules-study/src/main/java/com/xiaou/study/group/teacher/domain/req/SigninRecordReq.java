package com.xiaou.study.group.teacher.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 签到记录表
 * @TableName u_signin_record
 */
@Data
@AutoMapper(target = SigninRecord.class)
public class SigninRecordReq {

    /**
     * 签到任务ID，关联u_signin表
     */
    private String signinId;

    /**
     * 这个前端是一定会返回的
     */
    private Integer type;

    /**
     * 签到密码，仅密码签到时使用
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

}