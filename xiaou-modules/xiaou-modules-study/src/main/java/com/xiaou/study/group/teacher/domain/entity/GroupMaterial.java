package com.xiaou.study.group.teacher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 组资料表，存储组上传的各类资料
 * @TableName u_group_material
 */
@TableName(value ="u_group_material")
@Data
public class GroupMaterial {
    /**
     * 资料ID，主键UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 组ID，关联组表
     */
    private String groupId;

    /**
     * 上传人ID，为教师Id
     */
    private String uploaderId;

    /**
     * 资料标题
     */
    private String title;

    /**
     * 资料描述
     */
    private String description;

    /**
     * 资料文件地址，支持多个文件，JSON数组格式
     */
    private List<String> fileUrls;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date uploadedAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}