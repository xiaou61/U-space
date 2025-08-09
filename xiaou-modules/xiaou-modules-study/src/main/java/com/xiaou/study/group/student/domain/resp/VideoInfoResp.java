package com.xiaou.study.group.student.domain.resp;

import com.xiaou.study.group.student.domain.entity.VideoInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 入学必看视频信息表
 * @TableName u_video_info
 */
@Data
@AutoMapper(target = VideoInfo.class)
public class VideoInfoResp {
    /**
     * 视频ID
     */
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
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;
}