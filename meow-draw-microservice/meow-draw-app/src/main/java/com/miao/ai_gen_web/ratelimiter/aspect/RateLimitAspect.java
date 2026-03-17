package com.miao.ai_gen_web.ratelimiter.aspect;

import com.miao.ai_gen_web.entity.User;
import com.miao.ai_gen_web.exception.BusinessException;
import com.miao.ai_gen_web.exception.ErrorCode;
import com.miao.ai_gen_web.innerservice.InnerUserService;
import com.miao.ai_gen_web.ratelimiter.annotation.RateLimit;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 限流切面：拦截带有 @RateLimit 注解的方法
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    @Resource
    private RedissonClient redissonClient; // 注入 Redisson 客户端，用于操作 Redis

    @Resource
    @Lazy
    private InnerUserService userService; // 用于获取当前登录用户信息

    /**
     * 在方法执行之前进行限流检查
     * @param point 切入点
     * @param rateLimit 注解对象，包含限流配置参数
     */
    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        // 1. 根据切入点信息和注解配置，动态生成 Redis 里的唯一标识 key
        String key = generateRateLimitKey(point, rateLimit);

        // 2. 获取 Redisson 分布式限流器实例（如果 Redis 中不存在则会创建）
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 3. 设置限流器的过期时间（防止长期不使用的限流 key 占用 Redis 内存）
        rateLimiter.expire(Duration.ofHours(1));

        // 4. 初始化限流参数：
        // RateType.OVERALL 表示全局限流（所有节点共享此限流配置）
        // rateLimit.rate(): 允许的请求数
        // rateLimit.rateInterval(): 时间窗口长度
        // RateIntervalUnit.SECONDS: 时间单位（秒）
        // trySetRate 是原子操作，如果已经设置过且参数没变，则不会重复覆盖
        rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.rateInterval(), RateIntervalUnit.SECONDS);

        // 5. 尝试获取 1 个令牌
        // tryAcquire() 是非阻塞的，获取到令牌返回 true，否则立即返回 false
        if (!rateLimiter.tryAcquire(1)) {
            // 获取失败，说明触发了限流，直接抛出业务异常，阻断后续业务执行
            log.warn("限流触发: key = {}", key);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }
    }

    /**
     * 生成限流 key：决定了限流的颗粒度（是针对接口、针对人、还是针对 IP）
     */
    private String generateRateLimitKey(JoinPoint point, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");

        // 如果注解中配置了自定义 key 前缀，则拼接到 key 中
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        }

        // 根据限流类型（limitType）生成不同的后缀
        switch (rateLimit.limitType()) {
            case API:
                // 接口级别限流：key = 类名.方法名
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;
            case USER:
                // 用户级别限流：根据用户 ID 限流
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        User loginUser = InnerUserService.getLoginUser(request); // 获取当前登录人
                        keyBuilder.append("user:").append(loginUser.getId());
                    } else {
                        // 拿不到请求上下文（比如异步线程调用），降级为 IP 限流
                        keyBuilder.append("ip:").append(getClientIP());
                    }
                } catch (BusinessException e) {
                    // 用户未登录（抛出异常时），降级为 IP 限流
                    keyBuilder.append("ip:").append(getClientIP());
                }
                break;
            case IP:
                // IP 级别限流：同一个 IP 限制访问频率
                keyBuilder.append("ip:").append(getClientIP());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    /**
     * 工具方法：从请求头中获取真实的客户端 IP 地址
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        // 考虑负载均衡、代理等情况，依次从不同 Header 获取
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果经过多层代理，X-Forwarded-For 会有多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}