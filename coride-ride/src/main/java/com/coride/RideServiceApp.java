package com.coride;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableScheduling
@EnableFeignClients
public class RideServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RideServiceApp.class, args);
        log.info("Ride service server started");
    }
}
