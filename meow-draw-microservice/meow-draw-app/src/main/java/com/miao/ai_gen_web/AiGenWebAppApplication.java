package com.miao.ai_gen_web;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.miao.ai_gen_web.mapper")
@EnableDubbo
@EnableCaching
public class AiGenWebAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiGenWebAppApplication.class, args);
    }
}
