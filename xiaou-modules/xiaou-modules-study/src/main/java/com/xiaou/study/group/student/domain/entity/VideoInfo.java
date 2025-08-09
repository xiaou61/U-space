package com.xiaou.study.group.student.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 入学必看视频信息表
 * @TableName u_video_info
 */
@TableName(value ="u_video_info")
@Data
public class VideoInfo {
    /**
     * 视频ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频播放地址
     */
    private String url;

    /**
     * 封面图地址
     */
    private String coverUrl;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 视频简介
     */
    private String description;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}