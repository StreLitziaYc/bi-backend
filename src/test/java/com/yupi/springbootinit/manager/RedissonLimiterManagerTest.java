package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RedissonLimiterManagerTest {
    @Resource
    private RedissonLimiterManager redissonLimiterManager;


    @Test
    void doRateLimit() {
        String key = "1";
        for (int i = 0; i < 5; i++) {
            redissonLimiterManager.doRateLimit(key);
            System.out.println("Success");
        }
    }
}