package com.miao.ai_gen_web.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.route-chat-model")
@Data
public class RouteChatModelConfig {
    private String baseUrl;

    private String apiKey;

    private Duration timeout;

    private String modelName;

    @Bean
    public ChatModel routeChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .maxTokens(8192)
                .timeout(timeout)
                .build();
    }
}