package com.xiaou.notify.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.notify.domain.NotificationVo;
import com.xiaou.notify.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RequestMapping("/notify")
@RestController
public class NotifyController {
    @Resource
    private NotificationService notificationService;

    /**
     * 查看通知分页
     */
    @PostMapping("/page")
    private R<PageRespDto<NotificationVo>> getNotifyPage(@RequestBody PageReqDto dto){
        return notificationService.getNotifyPage(dto);
    }

}
