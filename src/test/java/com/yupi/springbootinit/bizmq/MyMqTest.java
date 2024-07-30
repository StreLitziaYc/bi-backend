package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootTest
class MyMqTest {
    private final String EXCHANGE_NAME = "test_exchange";
    private final String ROUTING_KEY = "test_routingKey";
    private final String QUEUE_NAME = "test_queue";
    @Autowired
    private BiMessageProducer messageProducer;

    @Autowired
    private BiMessageConsumer messageConsumer;

    // @Test
    void mqInit() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendMessage() {
        messageProducer.sendMessage(EXCHANGE_NAME, ROUTING_KEY, "hello world");
    }
}