package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * 分析生成的结果
 */
@Data
public class BiResultVO {
    /**
     * 生成的图表内容
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 新生成的图表id
     */
    private Long chatId;
}
