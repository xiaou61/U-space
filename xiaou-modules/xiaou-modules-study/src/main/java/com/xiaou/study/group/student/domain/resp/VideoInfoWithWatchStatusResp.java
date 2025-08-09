package com.xiaou.study.group.student.domain.resp;

import com.xiaou.study.group.student.domain.entity.VideoInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 带观看状态的入学必看视频信息响应类
 */
@Data
@AutoMapper(target = VideoInfoResp.class)
public class VideoInfoWithWatchStatusResp  {

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


    //新加字段
    /**
     * 当前用户是否已观看（0=否，1=是）
     */
    private Integer isWatched;

    /**
     * 观看进度（秒）
     */
    private Integer watchProgress;

    /**
     * 观看次数
     */
    private Integer watchCount;
}