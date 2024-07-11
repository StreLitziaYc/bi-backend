package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel处理工具
 */
@Slf4j
public class ExcelUtils {
    /**
     * 压缩excel并转化为csv
     *
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        // 读取数据
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误：", e);
        }
        // 如果数据为空
        if (CollUtil.isEmpty(list)) return "";
        // 转为CSV
        StringBuilder stringBuilder = new StringBuilder();
        list.forEach(dataMap -> stringBuilder.append(StringUtils.join(dataMap.values().stream().filter(ObjectUtils::isNotEmpty).toList(), ","))
                .append("\n"));
        return stringBuilder.toString();
    }
}
