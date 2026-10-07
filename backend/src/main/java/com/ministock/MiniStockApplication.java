package com.ministock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ministock.mapper")
public class MiniStockApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniStockApplication.class, args);
    }
}