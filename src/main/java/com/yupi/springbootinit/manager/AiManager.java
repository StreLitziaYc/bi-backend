package com.yupi.springbootinit.manager;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.vo.BiResultVO;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ai模型调用
 */
@Slf4j
@Data
public class AiManager {

    private final Generation generation;
    private final GenerationParam param;
    private final List<Message> messages;

    public AiManager(String modelName) {
        generation = new Generation();
        messages = new ArrayList<>();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                        "分析需求：\n" +
                        "{数据分析的需求或者目标}\n" +
                        "原始数据：\n" +
                        "{csv格式的原始数据，用,作为分隔符}\n" +
                        "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                        "$$$$$\n" +
                        "{前端Echarts V5 的 option 配置对象js代码，合理的将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                        "$$$$$\n" +
                        "{明确的数据分析结论、越详细越好，不要生成多余的注释}")
                .build();
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content("""
                        分析需求：
                        分析网站用户的增长情况
                        原始数据：
                        日期,用户数
                        1号,10
                        2号,20
                        3号,30
                        """)
                .build();
        Message assistantMessage = Message.builder()
                .role(Role.ASSISTANT.getValue())
                .content("""
                        %%%%%
                        {
                            "title": {
                                "text": "网站用户增长情况",
                                "subtext": ""
                                },
                            "tooltip": {
                                "trigger": "axis",
                                "axisPointer": {
                                    "type": "shadow"
                                    }
                            },
                            "legend": {
                                "data": ["用户数"]
                                },
                            "xAxis": {
                                "data": ["1号", "2号", "3号"]
                            },
                            "yAxis": {},
                            "series": [{
                                "name": "用户数",
                                "type": "bar",
                                "data": [10, 20, 30]
                            }]
                        }
                        %%%%%
                        根据数据分析可得，该网站用户数量逐日增长，时间越长，用户数量增长越多。""")
                .build();
        messages.add(systemMsg);
        messages.add(userMessage);
        messages.add(assistantMessage);
        param = GenerationParam.builder()
                .model(modelName)
                .messages(messages)
                .build();
        log.info("模型已完成初始化");
    }

    public static String getUserInput(MultipartFile multipartFile, String goal, String chartType) {
        String excelContent = ExcelUtils.excelToCsv(multipartFile);
        return getUserInput(excelContent, goal, chartType);
    }

    public static String getUserInput(String excelContent, String goal, String chartType) {
        if (StringUtils.isNotBlank(chartType)) goal += "，请使用" + chartType;
        return "分析需求：\n" +
                goal + "\n" +
                "原始数据：\n" +
                excelContent + "\n";
    }

    public BiResultVO doChat(String message) {
        log.info("用户提问：{}", message);
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(message)
                .build();
        List<Message> curMessages = new ArrayList<>(messages);
        curMessages.add(userMessage);
        param.setMessages(curMessages);
        GenerationResult generationResult;
        try {
            generationResult = generation.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error("AI模型调用错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI模型调用错误");
        }
        String stringResult = generationResult.getOutput().getChoices().get(0).getMessage().getContent();
        log.info("AI生成结果：{}", stringResult);
        return parseString2BiRes(stringResult);
    }

    private BiResultVO parseString2BiRes(String string) {
        String[] splits = string.split("%%%%%");
        ThrowUtils.throwIf(splits.length < 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        BiResultVO biResultVO = new BiResultVO();
        biResultVO.setGenChart(splits[1].trim());
        biResultVO.setGenResult(splits[2].trim());
        return biResultVO;
    }
}
