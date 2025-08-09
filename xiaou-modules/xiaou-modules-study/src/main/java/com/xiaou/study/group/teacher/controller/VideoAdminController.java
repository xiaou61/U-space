package com.xiaou.study.group.teacher.controller;

import com.xiaou.common.domain.R;
import com.xiaou.study.group.student.domain.req.VideoInfoReq;
import com.xiaou.study.group.student.domain.resp.VideoInfoResp;
import com.xiaou.study.group.student.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入学培训视频管理
 */
@RestController
@RequestMapping("/admin/video")
public class VideoAdminController {
    @Resource
    private VideoInfoService videoInfoService;


    @PostMapping("/add")
    public R<String> add(@RequestBody VideoInfoReq req) {
        return videoInfoService.add(req);
    }

    @PostMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return videoInfoService.delete(id);
    }

    @PostMapping("/list")
    public R<List<VideoInfoResp>> list() {
        return videoInfoService.listAll();
    }
}
