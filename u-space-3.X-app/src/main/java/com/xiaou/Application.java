package com.xiaou;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.xiaou"})
@Configurable
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.xiaou"})
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
