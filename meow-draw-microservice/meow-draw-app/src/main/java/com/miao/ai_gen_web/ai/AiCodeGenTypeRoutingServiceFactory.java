package com.miao.ai_gen_web.ai;

import com.miao.ai_gen_web.ai.AiCodeGenTypeRoutingService;
import com.miao.ai_gen_web.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author miao
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AI代码生成类型路由服务实例
     */

    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        ChatModel routeCM = SpringContextUtil.getBean("routeCMPrototype",ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routeCM)
                .build();
    }
}
