package com.xiaou.campus.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;

import lombok.Data;

@Data
@TableName("u_campus_guide")
public class CampusGuide {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /**
     * 图片 URL 列表，JSON字符串格式
     */
    @TableField("image_list")
    private String imageList;

    /**
     * 关键词列表，JSON字符串格式
     */
    private String keywords;

    private String category;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除字段，0正常，1删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

}
