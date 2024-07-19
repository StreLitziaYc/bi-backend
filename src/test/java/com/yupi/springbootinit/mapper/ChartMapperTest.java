package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.utils.ExcelUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartMapperTest {
    @Resource
    ChartMapper chartMapper;

    // @Test
    void createChartData() {
        Long id = 0L;
        File file = ExcelUtils.readLocalFile("test_excel.xlsx");
        List<String> headerList = ExcelUtils.getHeaderList(file);
        List<List<String>> dataList = ExcelUtils.getDataList(file);
        chartMapper.createChartData(id, headerList);
        dataList.forEach(data -> chartMapper.insertChartData(id, headerList, data));
    }
}