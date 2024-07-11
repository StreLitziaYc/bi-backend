package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 编辑请求
 */
@Data
public class ChartEditRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 图标名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表数据
     */
    private String chartData;
    /**
     * 图表类型
     */
    private String chartType;
}