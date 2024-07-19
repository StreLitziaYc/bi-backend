package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author Strelitzia
* @description 针对表【chart(图标信息表)】的数据库操作Mapper
* @createDate 2024-07-09 16:20:18
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {
    Integer createChartData(@Param("chartId") Long chartId, @Param("headerList") List<String> headerList);

    Integer insertChartData(@Param("chartId") Long chartId, @Param("headerList") List<String> headerList, @Param("dataList") List<String> dataList);
    @MapKey("id")
    List<Map<String, Object>> queryChartData(@Param("chartId") Long chartId);
}




