package com.xiaou.study.group.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.student.domain.entity.VideoWatchRecord;
import com.xiaou.study.group.student.domain.req.VideoWatchRecordReq;
import com.xiaou.study.group.student.domain.resp.VideoInfoWithWatchStatusResp;

import java.util.List;

public interface VideoWatchRecordService extends IService<VideoWatchRecord> {

    R<String> record(VideoWatchRecordReq req);

    /**
     * 获取所有必看视频列表（包含当前用户观看状态）
     */
    R<List<VideoInfoWithWatchStatusResp>> getVideosWithWatchStatus();
}
