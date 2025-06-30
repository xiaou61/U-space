package com.xiaou;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFileStorage
public class USpaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(USpaceApplication.class, args);
    }
}
