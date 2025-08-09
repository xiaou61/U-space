package com.xiaou.study.group.student.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.student.domain.entity.VideoWatchRecord;
import com.xiaou.study.group.student.domain.req.VideoWatchRecordReq;
import com.xiaou.study.group.student.domain.resp.VideoInfoResp;
import com.xiaou.study.group.student.domain.resp.VideoInfoWithWatchStatusResp;
import com.xiaou.study.group.student.mapper.VideoWatchRecordMapper;
import com.xiaou.study.group.student.service.VideoInfoService;
import com.xiaou.study.group.student.service.VideoWatchRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VideoWatchRecordServiceImpl extends ServiceImpl<VideoWatchRecordMapper, VideoWatchRecord>
        implements VideoWatchRecordService {

    @Resource
    private LoginHelper loginHelper;
    
    @Resource
    private VideoInfoService videoInfoService;

    @Override
    public R<String> record(VideoWatchRecordReq req) {
        VideoWatchRecord record = MapstructUtils.convert(req, VideoWatchRecord.class);
        record.setStudentId(loginHelper.getCurrentAppUserId());
        record.setWatchTime(new Date());
        record.setIsFinished(req.getIsFinished());
        return baseMapper.insertOrUpdate(record) ? R.ok("保存成功") : R.fail("保存失败");
    }
    
    @Override
    public R<List<VideoInfoWithWatchStatusResp>> getVideosWithWatchStatus() {
        try {
            // 获取当前用户ID
            String currentUserId = loginHelper.getCurrentAppUserId();
            
            // 获取所有视频信息
            R<List<VideoInfoResp>> videoListResult = videoInfoService.listAll();
            if (videoListResult.getCode() != 200 || videoListResult.getData() == null) {
                return R.fail("获取视频列表失败");
            }
            
            List<VideoInfoResp> videoList = videoListResult.getData();
            
            // 获取当前用户的观看记录
            QueryWrapper<VideoWatchRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("student_id", currentUserId);
            List<VideoWatchRecord> watchRecords = baseMapper.selectList(queryWrapper);
            
            // 将观看记录转换为Map，以videoId为key
            Map<String, VideoWatchRecord> watchRecordMap = watchRecords.stream()
                    .collect(Collectors.toMap(VideoWatchRecord::getVideoId, record -> record, (existing, replacement) -> existing));
            
            // 组装带观看状态的视频列表
            List<VideoInfoWithWatchStatusResp> result = videoList.stream()
                    .map(video -> {
                        VideoInfoWithWatchStatusResp resp = MapstructUtils.convert(video, VideoInfoWithWatchStatusResp.class);
                        
                        // 查找对应的观看记录
                        VideoWatchRecord watchRecord = watchRecordMap.get(video.getId());
                        if (watchRecord != null) {
                            resp.setIsWatched(watchRecord.getIsFinished() != null ? watchRecord.getIsFinished() : 0);
                            resp.setWatchProgress(watchRecord.getWatchProgress() != null ? watchRecord.getWatchProgress() : 0);
                            resp.setWatchCount(watchRecord.getWatchCount() != null ? watchRecord.getWatchCount() : 0);
                        } else {
                            resp.setIsWatched(0);
                            resp.setWatchProgress(0);
                            resp.setWatchCount(0);
                        }
                        
                        return resp;
                    })
                    .collect(Collectors.toList());
            
            return R.ok("获取成功", result);
        } catch (Exception e) {
            return R.fail("获取视频列表失败：" + e.getMessage());
        }
    }
}




