package com.xiaou.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.notify.domain.Notification;
import com.xiaou.notify.mapper.NotificationMapper;
import com.xiaou.notify.service.NotificationService;
import org.springframework.stereotype.Service;


@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
    implements NotificationService {

}




