package com.miao.ai_gen_web.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public class ReasoningStreamingChatModelConfig {
    private String baseUrl;

    private String apiKey;

    private Duration timeout;


    @Bean
    public StreamingChatModel reasoningStreamingChatModel(){
        final String modelname = "deepseek-chat";
        final int maxtokens = 8192;
//        final String modelname = "deepseek-reasoner";
//        final int maxtokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelname)
                .maxTokens(maxtokens)
                .logRequests(true)
                .logResponses(true)
                .timeout(timeout)
                .build();
    }

}
