package com.xiaou.web.controller;



import cn.hutool.extra.spring.SpringUtil;
import com.xiaou.common.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@RestController
public class IndexController {


    @Resource
    private RequestMappingHandlerMapping handlerMapping;

    @GetMapping("/")
    public String index() {
        return StringUtils.format("欢迎使用{}，请通过前端地址访问。", SpringUtil.getApplicationName());
    }




}
