package com.xiaou.system.bulletin.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.sse.dto.SseMessageDto;
import com.xiaou.sse.utils.SseMessageUtils;
import com.xiaou.system.bulletin.domain.entity.Announcement;
import com.xiaou.system.bulletin.domain.req.AnnouncementReq;
import com.xiaou.system.bulletin.mapper.AnnouncementMapper;
import com.xiaou.system.bulletin.service.AnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
    implements AnnouncementService {

    private final String ANNOUNCEMENT="announcement";



    @Override
    public R<String> add(AnnouncementReq req) {
        baseMapper.insert(MapstructUtils.convert(req, Announcement.class));

        SseMessageDto sseMessageDto = new SseMessageDto();
        sseMessageDto.setMessage(req.getContent());
        sseMessageDto.setType(ANNOUNCEMENT);

        // 群发给所有用户
        SseMessageUtils.publishAll(sseMessageDto);

        return R.ok("添加成功");
    }

}




