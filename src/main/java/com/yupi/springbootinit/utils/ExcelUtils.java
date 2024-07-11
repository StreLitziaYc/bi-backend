package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Excel处理工具
 */
@Slf4j
public class ExcelUtils {
    public static void validate(MultipartFile multipartFile) {
        // 校验文件大小，大于1MB抛出异常
        final long TEN_MB = 10L * 1024 * 1024;
        ThrowUtils.throwIf(multipartFile.getSize() > TEN_MB, ErrorCode.SYSTEM_ERROR, "文件大小超过10MB");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "csv", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.SYSTEM_ERROR, "不支持的文件后缀");
    }
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
