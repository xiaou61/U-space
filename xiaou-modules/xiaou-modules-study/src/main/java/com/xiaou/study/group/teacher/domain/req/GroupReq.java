package com.xiaou.study.group.teacher.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.study.group.teacher.domain.entity.Group;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 群组表
 * @TableName u_group
 */
@Data
@AutoMapper(target = Group.class)
public class GroupReq {


    /**
     * 群组名称
     */
    @NotBlank(message = "群组名称不能为空")
    private String name;

    /**
     * 群组描述
     */
    @NotBlank(message = "群组描述不能为空")
    private String description;

    /**
     * 创建人ID（老师）
     */
    @NotBlank(message = "创建人ID不能为空")
    //这里还需要校验是否为管理员或者老师才可以允许她创建
    private String creatorId;

}