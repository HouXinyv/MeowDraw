package com.miao.ai_gen_web.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.miao.ai_gen_web.ai.AiCodeGeneratorService;
import com.miao.ai_gen_web.ai.guardrail.PromptSafetyInputGuardrail;
import com.miao.ai_gen_web.ai.guardrail.RetryOutputGuardrail;
import com.miao.ai_gen_web.ai.tools.ToolManager;
import com.miao.ai_gen_web.exception.BusinessException;
import com.miao.ai_gen_web.exception.ErrorCode;
import com.miao.ai_gen_web.model.enums.CodeGenTypeEnum;
import com.miao.ai_gen_web.service.ChatHistoryService;
import com.miao.ai_gen_web.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType){
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey,key -> createAiCodeGeneratorService(
                appId,codeGenType
        ));
    }

    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType){
        return appId + "_" + codeGenType.getValue();
    }


    /**
     * 创建新的 AI 服务实例
     * @param appId 应用id
     * @param codeGenTypeEnum 生成应用类型
     */
    /**
     * 创建新的 AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(50)
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        // 根据代码生成类型选择不同的模型配置
        return switch (codeGenType) {
            // Vue 项目生成使用推理模型
            case VUE_PROJECT -> {
                StreamingChatModel codeRSCM = SpringContextUtil.getBean("codeRSCMPrototype",StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(codeRSCM)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(
                            toolManager.getAllTools()
                    )
                    .inputGuardrails(new PromptSafetyInputGuardrail())
                    .outputGuardrails(new RetryOutputGuardrail())
                    .maxSequentialToolsInvocations(35)
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                    ))// 出现幻觉的时候，工具不存在的情况时，做什么
                    .build();
            }


            // HTML 和多文件生成使用默认模型
            case HTML, MULTI_FILE -> {
                StreamingChatModel codeSCM = SpringContextUtil.getBean("codeSCMPrototype",StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(codeSCM)
                    .inputGuardrails(new PromptSafetyInputGuardrail())
                    .outputGuardrails(new RetryOutputGuardrail())
                    .maxSequentialToolsInvocations(15)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的代码生成类型: " + codeGenType.getValue());
        };
    }

}


