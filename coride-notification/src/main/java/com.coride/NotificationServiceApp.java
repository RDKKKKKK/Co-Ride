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
public class NotificationServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApp.class, args);
        log.info("User service server started");
    }
}
