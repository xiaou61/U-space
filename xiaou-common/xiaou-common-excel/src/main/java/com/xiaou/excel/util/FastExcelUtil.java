package com.xiaou.excel.util;


import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;

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
