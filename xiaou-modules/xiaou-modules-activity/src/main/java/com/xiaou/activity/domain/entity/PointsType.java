package com.xiaou.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 积分类型表
 * @TableName u_points_type
 */
@TableName(value = "u_points_type")
@Data
public class PointsType {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 积分类型名称
     */
    private String typeName;

    /**
     * 积分类型编码
     */
    private String typeCode;

    /**
     * 积分类型描述
     */
    private String description;

    /**
     * 积分图标URL
     */
    private String iconUrl;

    /**
     * 是否启用(0:禁用 1:启用)
     */
    private Integer isActive;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
} 