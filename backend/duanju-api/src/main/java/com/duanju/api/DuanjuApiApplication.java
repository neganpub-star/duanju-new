package com.duanju.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.duanju")
@MapperScan("com.duanju.**.mapper")
public class DuanjuApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DuanjuApiApplication.class, args);
    }
}
