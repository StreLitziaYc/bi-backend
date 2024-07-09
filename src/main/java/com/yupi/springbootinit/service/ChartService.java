package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;

/**
 * @author Strelitzia
 * @description 针对表【chart(图标信息表)】的数据库操作Service
 * @createDate 2024-07-09 16:20:18
 */
public interface ChartService extends IService<Chart> {

    void validChart(Chart chart, boolean add);

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
