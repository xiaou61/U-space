package com.xiaou.order.controller;

import com.xiaou.api.response.Response;
import com.xiaou.order.feign.UserServiceFeign;
import com.xiaou.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务控制器
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Response<Object> createOrder(@RequestParam String userId, @RequestParam String productId) {
        log.info("创建订单, userId: {}, productId: {}", userId, productId);
        
        // 调用用户服务获取用户信息
        Response<Object> userInfo = userServiceFeign.getUserInfo(userId);
        log.info("获取到用户信息: {}", userInfo.getData());
        
        // 模拟订单创建逻辑
        String orderId = "ORDER_" + System.currentTimeMillis();
        
        return Response.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("订单创建成功, orderId: " + orderId + ", userInfo: " + userInfo.getData())
                .build();
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Response<Object> getOrderDetail(@PathVariable String orderId) {
        log.info("获取订单详情, orderId: {}", orderId);
        
        return Response.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("订单详情: orderId=" + orderId + ", status=已支付, amount=99.99")
                .build();
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Response<Object> health() {
        return Response.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("Order Service is running")
                .data(System.currentTimeMillis())
                .build();
    }
} 