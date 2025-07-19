package com.xiaou.room.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.room.domain.entity.DormRegister;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 宿舍信息登记表
 * @TableName u_dorm_register
 */
@Data
@AutoMapper(target = DormRegister.class)
public class DormRegisterReq {


    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;

    /**
     * 身份证号码（支持合法性校验）
     */

    private String idCard;
}