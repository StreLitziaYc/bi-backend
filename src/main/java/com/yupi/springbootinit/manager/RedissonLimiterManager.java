package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import lombok.Data;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConfigurationProperties(prefix = "spring.redisson.limiter")
@Data
public class RedissonLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    private Integer maxRate;

    /**
     * 限流操作
     * @param key 区分不同的限流器，比如用户id
     */
    public void doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 每个时间单位允许5次请求，每个时间单位有1s
        rateLimiter.trySetRate(RateType.OVERALL, maxRate, 1, RateIntervalUnit.SECONDS);

        // 每当一个请求来了后，获取1个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        if (!canOp) throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
    }
}
