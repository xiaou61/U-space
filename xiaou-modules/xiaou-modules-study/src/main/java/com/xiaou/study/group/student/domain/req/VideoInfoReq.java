package com.xiaou.study.group.student.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class VideoInfoReq {

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
     * 视频简介
     */
    private String description;

}