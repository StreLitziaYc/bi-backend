package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartServiceImplTest {
    @Resource
    ChartService chartService;

    // @Test
    void createChartTable() {
        Long id = 0L;
        File file = ExcelUtils.readLocalFile("test_excel.xlsx");
        chartService.createChartTable(file, 0L);
    }
}