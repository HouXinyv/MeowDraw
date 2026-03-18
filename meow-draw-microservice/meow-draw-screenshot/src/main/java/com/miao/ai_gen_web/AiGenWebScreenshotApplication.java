package com.miao.ai_gen_web;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class AiGenWebScreenshotApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiGenWebScreenshotApplication.class, args);
    }
}
