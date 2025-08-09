package com.xiaou.auth.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统角色实体类
 * 
 * @author xiaou
 * @TableName sys_role
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role")
public class SysRole {

    /**
     * 角色ID，主键UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 角色代码，如：admin、teacher、bbs_admin
     */
    private String roleCode;

    /**
     * 角色名称，如：系统管理员、教师、BBS管理员
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统内置角色：1是 0否（系统角色不能删除）
     */
    private Boolean isSystem;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 状态：1启用 0禁用
     */
    private Boolean status;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}