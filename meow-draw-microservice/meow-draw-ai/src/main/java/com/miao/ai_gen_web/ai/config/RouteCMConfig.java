package com.miao.ai_gen_web.ai.config;

import com.miao.ai_gen_web.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.route-chat-model")
@Data
public class RouteCMConfig {
    private String baseUrl;

    private String apiKey;

    private Duration timeout;

    private String modelName;

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    @Bean
    @Scope("prototype")
    public ChatModel routeCMPrototype() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .maxTokens(8192)
                .timeout(timeout)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }
}