package com.miao.ai_gen_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient jdkHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))   // 建立连接
                // readTimeout 在 request 层控制（下面）
                .build();
    }

    @Bean
    public RestClient restClient(HttpClient httpClient) {
        return RestClient.builder()
                .requestFactory(
                        new JdkClientHttpRequestFactory(httpClient) {{
                            setReadTimeout(Duration.ofSeconds(900)); // ⭐⭐⭐ 核心
                        }}
                )
                .build();
    }
}