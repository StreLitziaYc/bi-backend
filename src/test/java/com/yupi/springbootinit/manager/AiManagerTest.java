package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class AiManagerTest {

    @Test
    void doChat() {
        AiManager aiManager = new AiManager("qwen-long");
        String res = aiManager.doChat("""
                分析需求：
                分析网站用户的增长情况
                原始数据：
                日期,用户数
                1号,30
                2号,20
                3号,10
                """);
        System.out.println(res);
    }
}