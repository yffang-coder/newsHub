package com.newshub.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.newshub.backend.infrastructure.persistence")
@EnableScheduling
public class NewshubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewshubBackendApplication.class, args);
    }

}
