package com.xiaou.excel.util;



import cn.hutool.core.io.IoUtil;
import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Excel 工具类，基于 FastExcel 封装
 */
@Slf4j
public class ExcelUtils {

    /**
     * 读取 Excel 数据（适用于本地文件上传）
     */
    public static <T> List<T> read(InputStream inputStream, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        FastExcel.read(inputStream, clazz, new ReadListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                list.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.info("Excel 解析完成：共 {} 条", list.size());
            }
        }).sheet().doRead();
        IoUtil.close(inputStream);
        return list;
    }

    /**
     * 导出 Excel 数据（适用于 Web 下载）
     */
    public static <T> void write(HttpServletResponse response, List<T> dataList, Class<T> clazz, String fileName, String sheetName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName + ".xlsx");

            FastExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(dataList);
        } catch (Exception e) {
            log.error("Excel导出失败", e);
        }
    }

}
