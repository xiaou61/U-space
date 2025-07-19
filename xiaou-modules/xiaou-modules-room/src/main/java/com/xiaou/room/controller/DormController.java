package com.xiaou.room.controller;

import com.xiaou.common.domain.R;
import com.xiaou.room.domain.req.DormRegisterReq;
import com.xiaou.room.service.DormRegisterService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dorm")
@Validated
public class DormController {
    @Resource
    private DormRegisterService dormRegisterService;
    /**
     * 判断用户是否需要输入信息
     * false为不需要 true为需要
     */
    @GetMapping("/isNeedInputInfo")
    public R<Boolean> isNeedInputInfo() {
        return dormRegisterService.isNeedInputInfo();
    }
    /**
     * 输入信息
     */
    @PostMapping("/inputInfo")
    public R<String> inputInfo(@Validated DormRegisterReq req) {
        return dormRegisterService.inputInfo(req);
    }

    /**
     * 列出自己可选择的宿舍信息 聚合 这里要根据id获取性别然后返回的是自己的性别
     */
    @GetMapping("/list")
    public R<String> list() {
        return null;
    }




}
