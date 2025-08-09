package com.xiaou.study.group.student.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 入学必看视频观看记录表
 * @TableName u_video_watch_record
 */
@TableName(value ="u_video_watch_record")
@Data
public class VideoWatchRecord {
    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 最近一次观看时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date watchTime;

    /**
     * 观看进度（秒）
     */
    private Integer watchProgress;

    /**
     * 是否已完整观看（0=否，1=是）
     */
    private Integer isFinished;

    /**
     * 观看次数
     */
    private Integer watchCount;

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