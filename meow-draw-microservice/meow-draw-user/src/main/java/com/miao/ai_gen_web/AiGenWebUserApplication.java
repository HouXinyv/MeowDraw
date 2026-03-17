package com.miao.ai_gen_web;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.miao.ai_gen_web.mapper")
@ComponentScan("com.miao")
@EnableDubbo
public class AiGenWebUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiGenWebUserApplication.class, args);
    }
}
