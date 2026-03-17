package com.miao.ai_gen_web.exception;

import cn.hutool.json.JSONUtil;
import com.miao.ai_gen_web.common.BaseResponse;
import com.miao.ai_gen_web.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        // 尝试处理 SSE 请求
        if (handleSseError(e.getCode(), e.getMessage())) {
            return null;
        }
        // 对于普通请求，返回标准 JSON 响应
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        // 尝试处理 SSE 请求
        if (handleSseError(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误")) {
            return null;
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 处理 SSE 业务错误：将错误信息包装成 SSE 格式发送给前端
     * @param errorCode 错误码
     * @param errorMessage 错误描述
     * @return true 表示已按 SSE 格式处理并响应；false 表示非 SSE 请求，需按普通逻辑处理
     */
    private boolean handleSseError(int errorCode, String errorMessage) {
        // 1. 从 ThreadLocal 中获取当前线程绑定的请求属性对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false; // 如果获取不到（例如在非 Web 线程中执行），无法处理响应
        }

        // 2. 提取底层的原生 Servlet 请求和响应对象
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 3. 识别当前请求是否为 SSE 请求
        // 方式 A: 检查 HTTP 头的 Accept 是否明确要求 text/event-stream
        String accept = request.getHeader("Accept");
        // 方式 B: 根据业务约定的特定 URL 路径硬编码判断
        String uri = request.getRequestURI();

        if ((accept != null && accept.contains("text/event-stream")) ||
                uri.contains("/chat/gen/code")) {
            try {
                // 4. 【关键】强制修改响应状态和头信息，告诉浏览器：这是一个 SSE 流式响应
                // 注意：此时可能 Controller 还没报错，但我们要截断正常的业务流，返回错误流
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Cache-Control", "no-cache"); // 禁止浏览器缓存，确保消息实时性
                response.setHeader("Connection", "keep-alive"); // 保持长连接

                // 5. 构造业务错误数据（Map 结构）
                Map<String, Object> errorData = Map.of(
                        "error", true,
                        "code", errorCode,
                        "message", errorMessage
                );
                // 使用 Hutool 等工具类将对象转为 JSON 字符串
                String errorJson = JSONUtil.toJsonStr(errorData);

                // 6. 按照 SSE 标准规范拼接字符串
                // event: 定义事件名称（前端用 eventSource.addEventListener('business-error', ...) 监听）
                // data: 定义消息体（必须以 \n\n 结尾表示一条消息结束）
                String sseData = "event: business-error\ndata: " + errorJson + "\n\n";

                // 7. 写入缓冲区并立即冲刷到网络缓冲区中，发送给客户端
                response.getWriter().write(sseData);
                response.getWriter().flush();

                // 8. 发送一个约定的结束标志位（done 事件），防止客户端一直处于连接等待状态
                response.getWriter().write("event: done\ndata: {}\n\n");
                response.getWriter().flush();

                // 标识当前请求已经被我们接管并成功处理了
                return true;
            } catch (IOException ioException) {
                // 如果在写入过程中连接断开，记录日志
                log.error("Failed to write SSE error response", ioException);
                // 即使失败也返回 true，因为该请求已经确认为 SSE，不需要再走普通的 JSON/HTML 错误返回
                return true;
            }
        }
        // 如果不是 SSE 请求，返回 false，后续流程可能会抛出普通 JSON 异常
        return false;
    }
}

