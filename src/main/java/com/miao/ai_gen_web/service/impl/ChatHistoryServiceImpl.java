package com.miao.ai_gen_web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.miao.ai_gen_web.constant.UserConstant;
import com.miao.ai_gen_web.entity.App;
import com.miao.ai_gen_web.entity.User;
import com.miao.ai_gen_web.exception.ErrorCode;
import com.miao.ai_gen_web.exception.ThrowUtils;
import com.miao.ai_gen_web.model.dto.chathistory.ChatHistoryQueryRequest;
import com.miao.ai_gen_web.model.enums.ChatHistoryMessageTypeEnum;
import com.miao.ai_gen_web.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.miao.ai_gen_web.entity.ChatHistory;
import com.miao.ai_gen_web.mapper.ChatHistoryMapper;
import com.miao.ai_gen_web.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author miao
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Autowired
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 1. 开始组装数据库查询条件（用的是 MyBatis-Flex 或类似框架）
            QueryWrapper queryWrapper = QueryWrapper.create()
                    // 只要这个 appId 的聊天记录
                    .eq(ChatHistory::getAppId, appId)
                    // 按创建时间倒序排（最晚聊的在最上面，比如：10:05, 10:04, 10:03...）
                    .orderBy(ChatHistory::getCreateTime, false)
                    // 骚操作：从第 1 条开始取，取 maxCount 条。目的是跳过第 0 条（即用户刚发的那条提问）
                    .limit(1, maxCount);

            // 2. 执行查询，把历史记录从数据库里捞出来变成一个 List 列表
            List<ChatHistory> historyList = this.list(queryWrapper);

            // 3. 如果数据库里啥也没有（新用户），直接返回 0，啥也不干
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }

            // 4. 【关键】反转列表。刚才捞出来是“新->旧”，现在反转成“旧->新”（比如：10:03, 10:04, 10:05）
            // 因为 AI 记忆必须按照说话的先后顺序塞进去，它才能理解上下文
            historyList = historyList.reversed();

            // 5. 初始化计数器，记录成功加载了多少条
            int loadedCount = 0;

            // 6. 【暴力清空】先把 AI 现在的脑子清空，防止新旧记忆混在一起乱套
            chatMemory.clear();

            // 7. 开始循环，把数据库里的每一条记录转换成 AI 能听懂的格式
            for (ChatHistory history : historyList) {
                // 如果这条记录类型是“用户(USER)”说的
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    // 把文字包装成 UserMessage 对象，塞进 AI 记忆
                    UserMessage msg = UserMessage.from(history.getMessage());
                    chatMemory.add(msg);
                    loadedCount++; // 计数+1
                }
                // 如果这条记录类型是“AI”说的
                else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    // 把文字包装成 AiMessage 对象，塞进 AI 记忆
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++; // 计数+1
                }
            }

            // 8. 打印个日志，炫耀一下成功恢复了多少条记忆
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;

        } catch (Exception e) {
            // 9. 万一数据库崩了或者代码报错，别让整个程序挂掉，记个错误日志，返回 0
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            return 0;
        }
    }

}
