package com.xiaou.study.group.teacher.domain.resp;

import com.xiaou.study.group.teacher.domain.entity.Signin;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 群组签到任务表
 * @TableName u_signin
 */

@Data
@AutoMapper(target = Signin.class)
public class SigninResp {

    private String id;

    /**
     * 群组ID，关联u_group表
     */
    private String groupId;

    /**
     * 签到类型：0=普通，1=密码
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
    private Date endTime;


    /**
     * 签到群组名字 需要填充的自断
     */
    private String groupName;

}