package com.xiaou.study.group.teacher.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;
import com.xiaou.study.group.teacher.serivce.SigninService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher/signin")
@Validated
public class SigninAdminController {
    @Resource
    private SigninService signinService;

    /**
     * 新建一个签到
     */
    @SaCheckRole(RoleConstant.TEACHER)
    @PostMapping("/add")
    private R<String> add(@RequestBody SigninReq req) {
        return signinService.add(req);
    }
    /**
     * 分页查看发布的签到 仅自己的
     */
    @PostMapping("/list")
    @SaCheckRole(RoleConstant.TEACHER)
    private R<PageRespDto<SigninResp>> list(@RequestBody PageReqDto req) {
        return signinService.PageList(req);
    }
}
