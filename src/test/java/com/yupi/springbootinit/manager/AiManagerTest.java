package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.model.vo.BiResultVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {
    @Resource
    private AiManager aiManager;

    //@Test
    void doChat() {
        BiResultVO biResultVO = aiManager.doChat("""
                分析需求：
                分析网站用户的增长情况
                原始数据：
                日期,用户数
                1号,30
                2号,20
                3号,10
                """);
        System.out.println(biResultVO.getGenChart());
        System.out.println(biResultVO.getGenResult());
    }
}