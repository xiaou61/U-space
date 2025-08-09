package com.xiaou.study.group.student.controller;

import com.xiaou.common.domain.R;
import com.xiaou.study.group.student.domain.entity.VideoWatchRecord;
import com.xiaou.study.group.student.domain.req.VideoWatchRecordReq;
import com.xiaou.study.group.student.domain.resp.VideoInfoWithWatchStatusResp;
import com.xiaou.study.group.student.service.VideoWatchRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/video-watch")
public class VideoWatchController {
    @Resource
    private VideoWatchRecordService videoWatchRecordService;

    /**
     * 添加一个记录
     */
    @PostMapping("/record")
    public R<String> addRecord(@RequestBody VideoWatchRecordReq req) {
        return videoWatchRecordService.record(req);
    }

    /**
     * 获取所有必看视频列表（包含当前用户观看状态）
     */
    @PostMapping("/list")
    public R<List<VideoInfoWithWatchStatusResp>> getVideosWithWatchStatus() {
        return videoWatchRecordService.getVideosWithWatchStatus();
    }
}
