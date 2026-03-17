package com.miao.ai_gen_web.config.model;

import com.miao.ai_gen_web.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class CodeCMConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private Double temperature;

    private Integer maxRetries;

    private boolean logRequests;

    private boolean logResponses;

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;

    @Bean
    // @Bean 的名字就是方法名, Bean 类型就是方法返回类型（以及它的实现类类型也能按实现查到
    @Scope("prototype")
    public ChatModel codeCMPrototype() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .maxRetries(maxRetries)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }
}
