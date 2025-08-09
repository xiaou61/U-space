package com.xiaou.study.group.student.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.study.group.student.domain.entity.VideoInfo;
import com.xiaou.study.group.student.domain.req.VideoInfoReq;
import com.xiaou.study.group.student.domain.resp.VideoInfoResp;
import com.xiaou.study.group.student.mapper.VideoInfoMapper;
import com.xiaou.study.group.student.service.VideoInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoInfoServiceImpl extends ServiceImpl<VideoInfoMapper, VideoInfo>
    implements VideoInfoService {

    @Override
    public R<String> add(VideoInfoReq req) {
        VideoInfo convert = MapstructUtils.convert(req, VideoInfo.class);
        baseMapper.insert(convert);
        return R.ok("添加成功");
    }

    @Override
    public R<String> delete(String id) {
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<List<VideoInfoResp>> listAll() {

        List<VideoInfo> videoInfoList = baseMapper.selectList(null);
        List<VideoInfoResp> convert = MapstructUtils.convert(videoInfoList, VideoInfoResp.class);
        return R.ok(convert);
    }
}




