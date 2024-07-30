package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.BiMqConstant;
import com.yupi.springbootinit.constant.TaskStatusConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.vo.BiResultVO;
import com.yupi.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    private void rejectMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息拒绝失败");
        }
    }
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (StringUtils.isBlank(message)) {
            rejectMessage(channel, deliveryTag);
        }
        log.info("receivedMessage message = {}", message);
        Long chartId = Long.valueOf(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) rejectMessage(channel, deliveryTag);
        try {
            // 先修改状态为 running
            chart.setStatus(TaskStatusConstant.RUNNING);
            boolean update1 = chartService.updateById(chart);
            if (!update1) rejectMessage(channel, deliveryTag);
            ThrowUtils.throwIf(!update1, ErrorCode.SYSTEM_ERROR, "图表信息更新失败");
            // 用户输入
            String userInput = AiManager.getUserInput(chartService.queryChartData(chartId), chart.getGoal(), chart.getChartType());
            // 生成结果
            BiResultVO biResult = aiManager.doChat(userInput);
//                biResult.setGenResult(result.getGenResult());
//                biResult.setGenChart(result.getGenChart());
            chart.setGenChart(biResult.getGenChart());
            chart.setGenResult(biResult.getGenResult());
            boolean saveUpdate = chartService.updateById(chart);
            if (!saveUpdate) rejectMessage(channel, deliveryTag);
            ThrowUtils.throwIf(!saveUpdate, ErrorCode.SYSTEM_ERROR, "生成图表保存失败");
            chart.setStatus(TaskStatusConstant.SUCCEED);
            boolean update2 = chartService.updateById(chart);
            if (!update2) rejectMessage(channel, deliveryTag);
            ThrowUtils.throwIf(!update2, ErrorCode.SYSTEM_ERROR, "图表信息更新失败");
        } catch (Exception e) {
            rejectMessage(channel, deliveryTag);
            chart.setExecMessage(e.getMessage());
            boolean update3 = chartService.updateById(chart);
            if (!update3) rejectMessage(channel, deliveryTag);
            ThrowUtils.throwIf(!update3, ErrorCode.SYSTEM_ERROR, "图表错误信息保存失败");
        }
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息确认失败");
        }
    }
}
