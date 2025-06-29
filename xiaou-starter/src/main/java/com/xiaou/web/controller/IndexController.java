package com.xiaou.web.controller;


import cn.hutool.extra.spring.SpringUtil;
import com.xiaou.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IndexController {



    @GetMapping("/")
    public String index() {
        return StringUtils.format("欢迎使用{}，请通过前端地址访问。", SpringUtil.getApplicationName());
    }




}
