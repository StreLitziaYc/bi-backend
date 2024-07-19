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
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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
     * 读取Excel文件
     *
     * @param multipartFile
     * @return
     */
    private static List<Map<Integer, String>> readExcel(MultipartFile multipartFile) {
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
        return list;
    }

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    private static List<Map<Integer, String>> readExcel(File file) {
        // 读取数据
        List<Map<Integer, String>> list = null;
        list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        return list;
    }

    /**
     * 压缩excel并转化为csv
     *
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        // 读取数据
        List<Map<Integer, String>> list = readExcel(multipartFile);
        return mapListToCsv(list);
    }

    public static <K, V> String mapListToCsv(List<Map<K, V>> mapList) {
        // 如果数据为空
        if (CollUtil.isEmpty(mapList)) return "";
        // 转为CSV
        StringBuilder stringBuilder = new StringBuilder();
        mapList.forEach(dataMap -> stringBuilder.append(StringUtils.join(
                        dataMap.values().stream().filter(ObjectUtils::isNotEmpty).map(V::toString).toList(),
                        ","))
                .append("\n"));
        return stringBuilder.toString();
    }

    public static List<String> getHeaderList(List<Map<Integer, String>> list) {
        // 如果数据为空
        if (CollUtil.isEmpty(list)) return Collections.emptyList();
        return list.get(0).values().stream().toList();
    }

    /**
     * 获取表头
     *
     * @param multipartFile
     * @return
     */
    public static List<String> getHeaderList(MultipartFile multipartFile) {
        return getHeaderList(readExcel(multipartFile));
    }

    public static List<String> getHeaderList(File file) {
        return getHeaderList(readExcel(file));
    }

    public static List<List<String>> getDataList(List<Map<Integer, String>> list) {
        // 如果数据为空
        if (CollUtil.isEmpty(list)) return Collections.emptyList();
        // 去除表头
        list.remove(0);
        List<List<String>> dataList = new ArrayList<>();
        list.forEach(dataMap -> dataList.add(dataMap.values().stream().toList()));
        return dataList;
    }

    /**
     * 获取表格数据
     *
     * @param multipartFile
     * @return
     */
    public static List<List<String>> getDataList(MultipartFile multipartFile) {
        return getDataList(readExcel(multipartFile));
    }

    public static List<List<String>> getDataList(File file) {
        return getDataList(readExcel(file));
    }

    /**
     * 读取本地文件
     *
     * @param fileName
     * @return
     */
    public static File readLocalFile(String fileName) {
        File file = null;
        try {
            file = ResourceUtils.getFile("classpath:" + fileName);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }
        return file;
    }
}
