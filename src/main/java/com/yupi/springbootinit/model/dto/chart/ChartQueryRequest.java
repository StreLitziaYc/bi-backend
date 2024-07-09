package com.yupi.springbootinit.model.dto.chart;

import com.baomidou.mybatisplus.annotation.TableField;
import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询请求
 */
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表类型
     */
    private String chartType;
    /**
     * 创建用户id
     */
    private Long userId;
}