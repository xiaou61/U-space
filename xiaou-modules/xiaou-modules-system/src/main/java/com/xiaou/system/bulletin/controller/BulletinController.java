package com.xiaou.system.bulletin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.system.bulletin.domain.entity.Announcement;
import com.xiaou.system.bulletin.domain.req.AnnouncementReq;
import com.xiaou.system.bulletin.service.AnnouncementService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bulletin")
@Validated
public class BulletinController {
    @Resource
    private AnnouncementService announcementService;

    /**
     * 添加公告 admin进行添加
     */
    @SaCheckRole(RoleConstant.ADMIN)
    @Log(title = "添加公告", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<String> add(@RequestBody AnnouncementReq req) {
        return announcementService.add(req);
    }
}
