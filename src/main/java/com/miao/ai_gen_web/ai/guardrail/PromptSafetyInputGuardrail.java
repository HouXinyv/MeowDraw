package com.miao.ai_gen_web.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 提示词安全输入护栏
 * 实现 InputGuardrail 接口，在用户消息发送给 AI 模型之前进行拦截校验
 */
public class PromptSafetyInputGuardrail implements InputGuardrail {

    // 1. 定义敏感词黑名单：涵盖了常见的攻击性词汇和试图引导 AI 违规的关键词
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above", // 典型的指令覆盖攻击
            "破解", "hack", "绕过", "bypass", // 尝试突破限制的词汇
            "越狱", "jailbreak" // 针对大模型的“越狱”手段
    );

    // 2. 定义正则表达式模式：用于识别更复杂、多变的提示词注入攻击（忽略大小写 (?i)）
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            // 匹配：要求忽略之前、上方或所有指令/命令/提示的语句
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            // 匹配：要求忘记、不顾之前或上方所有内容的语句
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            // 匹配：试图让 AI 角色扮演（伪装成其他身份来绕过安全限制）
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            // 匹配：伪造系统角色指令（System Prompt Injection）
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            // 匹配：试图强行开启“新指令”块的格式
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    /**
     * 核心校验方法
     * @param userMessage 用户发送给 AI 的原始消息对象
     * @return 校验结果（成功或致命错误）
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // 提取用户输入的纯文本内容
        String input = userMessage.singleText();

        // --- 校验逻辑 1：长度限制 ---
        // 防止超长文本攻击（可能会导致消耗过多 Token 或撑爆模型上下文窗口）
        if (input.length() > 1000) {
            return fatal("输入内容过长，不要超过 1000 字");
        }

        // --- 校验逻辑 2：空值检查 ---
        // 过滤无意义的空请求
        if (input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }

        // --- 校验逻辑 3：敏感词过滤 ---
        // 先统一转为小写，提高匹配成功率
        String lowerInput = input.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitiveWord.toLowerCase())) {
                // 如果命中黑名单，直接中断请求并返回警告信息
                return fatal("输入包含不当内容，请修改后重试");
            }
        }

        // --- 校验逻辑 4：正则攻击模式匹配 ---
        // 利用正则表达式检测更隐蔽的结构化注入攻击
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                // 如果发现匹配正则模式，判定为恶意攻击
                return fatal("检测到恶意输入，请求被拒绝");
            }
        }

        // --- 校验通过 ---
        // 所有检查项均正常，允许将消息发送给 AI 模型
        return success();
    }
}