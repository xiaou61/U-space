package com.xiaou.order.feign;

import com.xiaou.api.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 * @author xiaou
 */
@FeignClient(value = "u-space-user-service")
public interface UserServiceFeign {

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/info/{userId}")
    Response<Object> getUserInfo(@PathVariable("userId") String userId);
} 