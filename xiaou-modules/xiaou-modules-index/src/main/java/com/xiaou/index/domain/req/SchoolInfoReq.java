package com.xiaou.index.domain.req;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.index.domain.entity.SchoolInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 学校信息表，存储学校基本资料和定位信息
 * @TableName u_school_info
 */
@Data
@AutoMapper(target = SchoolInfo.class)
public class SchoolInfoReq {

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

}