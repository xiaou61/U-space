package com.xiaou.study.group.teacher.domain.req;

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
public class GroupMaterialReq {

    /**
     * 组ID，关联组表
     */
    private String groupId;

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

}