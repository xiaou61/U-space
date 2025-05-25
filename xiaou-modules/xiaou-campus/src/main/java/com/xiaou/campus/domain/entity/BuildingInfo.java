package com.xiaou.campus.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("u_building_info")
public class BuildingInfo {

    @TableId
    private Long id;

    private String name;

    private String aliases;

    private String img;

    private String images; // 存 JSON 字符串

    private String description;

    private Double latitude;

    private Double longitude;

    private Long categoryId;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
