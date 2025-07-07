package com.xiaou.study.group.teacher.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.study.group.teacher.domain.entity.GroupMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 组资料表，存储组上传的各类资料
 * @TableName u_group_material
 */
@Data
@AutoMapper(target = GroupMaterial.class)
public class GroupMaterialResp {
    /**
     * 资料ID，主键UUID
     */
    private String id;

    /**
     * 组ID，关联组表
     */
    private String groupId;

    /**
     * 组名字需要查找
     */
    private String groupName;

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
    private Date uploadedAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}