package com.xiaou.study.group.student.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.student.domain.entity.VideoInfo;
import com.xiaou.study.group.student.domain.req.VideoInfoReq;
import com.xiaou.study.group.student.domain.resp.VideoInfoResp;

import java.util.List;

public interface VideoInfoService extends IService<VideoInfo> {


    R<String> add(VideoInfoReq req);

    R<String> delete(String id);

    R<List<VideoInfoResp>> listAll();
}
