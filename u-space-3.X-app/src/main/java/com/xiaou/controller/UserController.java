package com.xiaou.controller;

import com.xiaou.api.response.Response;
import com.xiaou.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务控制器
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 获取用户信息
     */
    @GetMapping("/info/{userId}")
    public Response<Object> getUserInfo(@PathVariable String userId) {
        log.info("获取用户信息, userId: {}", userId);
        
        return Response.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("用户信息: userId=" + userId + ", name=张三, age=25")
                .build();
    }

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public Response<Object> createUser(@RequestBody String userData) {
        log.info("创建用户, userData: {}", userData);
        
        return Response.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("用户创建成功")
                .build();
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Response<Object> health() {
        return Response.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("User Service is running")
                .data(System.currentTimeMillis())
                .build();
    }
} 