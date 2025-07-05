package com.xiaou.study.group.teacher.domain.resp;

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
public class SigninRecordResp {


    /**
     * 签到人ID，关联u_student表
     */
    private String studentId;

    /**
     * 签到人姓名
     */
    private String studentName;

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

}