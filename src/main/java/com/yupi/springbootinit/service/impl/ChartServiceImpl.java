package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.sql.SQLSyntaxErrorException;
import java.util.List;

/**
* @author Strelitzia
* @description 针对表【chart(图标信息表)】的数据库操作Service实现
* @createDate 2024-07-09 16:20:18
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{
    @Resource
    ChartMapper chartMapper;

    @Override
    public void validChart(Chart chart, boolean add) {
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = chart.getId();
        String goal = chart.getGoal();
        String chartData = chart.getChartData();
        String chartType = chart.getChartType();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(goal, chartData, chartType) || id == null, ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "charType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void createChartTable(MultipartFile multipartFile, Long chartId) {
        List<String> headerList = ExcelUtils.getHeaderList(multipartFile);
        List<List<String>> dataList = ExcelUtils.getDataList(multipartFile);
        try {
            chartMapper.createChartData(chartId, headerList);
        } catch (BadSqlGrammarException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        dataList.forEach(data -> chartMapper.insertChartData(chartId, headerList, data));
    }

    @Override
    public void createChartTable(File file, Long chartId) {
        List<String> headerList = ExcelUtils.getHeaderList(file);
        List<List<String>> dataList = ExcelUtils.getDataList(file);
        try {
            chartMapper.createChartData(chartId, headerList);
        } catch (BadSqlGrammarException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
        dataList.forEach(data -> chartMapper.insertChartData(chartId, headerList, data));
    }

}




