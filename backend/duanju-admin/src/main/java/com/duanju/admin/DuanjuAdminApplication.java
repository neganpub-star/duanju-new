package com.duanju.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.duanju")
@MapperScan("com.duanju.**.mapper")
public class DuanjuAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(DuanjuAdminApplication.class, args);
    }
}
