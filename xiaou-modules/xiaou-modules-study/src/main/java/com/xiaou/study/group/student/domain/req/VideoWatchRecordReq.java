package com.xiaou.study.group.student.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.study.group.student.domain.entity.VideoWatchRecord;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 入学必看视频观看记录表
 *
 * @TableName u_video_watch_record
 */
@Data
@AutoMapper(target = VideoWatchRecord.class)
public class VideoWatchRecordReq {

    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 是否已完整观看（0=否，1=是）
     */
    private Integer isFinished;

}