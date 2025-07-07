package com.xiaou.index.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 学校信息表，存储学校基本资料和定位信息
 * @TableName u_school_info
 */
@TableName(value ="u_school_info")
@Data
public class SchoolInfo {
    /**
     * 学校ID，主键 UUID
     */
    @TableId
    private String id;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学校简介
     */
    private String description;

    /**
     * 学校Logo图片地址
     */
    private String logoUrl;

    /**
     * 学校官网链接
     */
    private String websiteUrl;

    /**
     * 学校详细地址
     */
    private String address;

    /**
     * 纬度，用于地图跳转
     */
    private BigDecimal latitude;

    /**
     * 经度，用于地图跳转
     */
    private BigDecimal longitude;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}