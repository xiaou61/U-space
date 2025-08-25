package com.xiaou.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 * @author xiaou
 */
@SpringBootApplication(scanBasePackages = {"com.xiaou"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.xiaou"})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
} 