package com.xiaou.excel.util;


import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import com.xiaou.common.utils.StringUtils;

import java.io.File;
import java.util.List;

/**
 * FastExcel 工具类（简化读取和写入）
 */
public class FastExcelUtil {

    /**
     * 读取 Excel 文件并返回 List<T>
     *
     * @param filePath   Excel 文件路径
     * @param clazz      目标实体类
     * @param <T>        泛型
     * @return List<T> 数据集合
     */
    public static <T> List<T> read(String filePath, Class<T> clazz) {
        ExcelListListener<T> listener = new ExcelListListener<>();
        FastExcel.read(filePath, clazz, listener).sheet().doRead();
        return listener.getDataList();
    }

    /**
     * 将数据写入 Excel 文件
     *
     * @param filePath   目标文件路径
     * @param clazz      数据类型
     * @param sheetName  Sheet 名称
     * @param dataList   写入数据
     * @param <T>        泛型
     */
    public static <T> void write(String filePath, Class<T> clazz, String sheetName, List<T> dataList) {
        FastExcel.write(filePath, clazz).sheet(sheetName).doWrite(dataList);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(StringUtils.SEPARATOR);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(StringUtils.SEPARATOR);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 内部类：收集数据的监听器
     */
    private static class ExcelListListener<T> implements ReadListener<T> {
        private final List<T> dataList = new java.util.ArrayList<>();

        @Override
        public void invoke(T data, AnalysisContext context) {
            dataList.add(data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            System.out.println("所有数据解析完成，总数：" + dataList.size());
        }

        public List<T> getDataList() {
            return dataList;
        }
    }
}
