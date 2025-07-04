package com.xiaou.system.bulletin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.system.bulletin.domain.entity.Announcement;
import com.xiaou.system.bulletin.domain.req.AnnouncementReq;

import java.util.List;


public interface AnnouncementService extends IService<Announcement> {


    R<String> add(AnnouncementReq req);

    R<List<Announcement>> listAll();
}
