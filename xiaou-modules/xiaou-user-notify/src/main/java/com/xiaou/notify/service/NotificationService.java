package com.xiaou.notify.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.notify.domain.Notification;
import com.xiaou.notify.domain.NotificationVo;


public interface NotificationService extends IService<Notification> {

    R<PageRespDto<NotificationVo>> getNotifyPage(PageReqDto dto);
}
