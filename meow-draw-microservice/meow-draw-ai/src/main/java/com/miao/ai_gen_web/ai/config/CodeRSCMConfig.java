package com.miao.ai_gen_web.ai.config;

//import com.miao.ai_gen_web.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.List;

@Configuration
@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public class CodeRSCMConfig {
    private String baseUrl;

    private String apiKey;

    private Duration timeout;

//    @Resource
//    private AiModelMonitorListener aiModelMonitorListener;

    @Bean
    @Scope("prototype")
    public StreamingChatModel codeRSCMPrototype(){
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
//                .listeners(List.of(aiModelMonitorListener))
                .build();
    }

}
