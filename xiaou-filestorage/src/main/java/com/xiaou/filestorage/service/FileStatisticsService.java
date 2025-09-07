package com.xiaou.filestorage.service;

import java.util.Map;

/**
 * 文件统计服务接口
 *
 * @author xiaou
 */
public interface FileStatisticsService {

    /**
     * 获取文件统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getFileStatistics();

    /**
     * 获取存储使用情况
     *
     * @return 存储使用情况
     */
    Map<String, Object> getStorageUsage();

    /**
     * 获取模块文件统计
     *
     * @return 模块统计信息
     */
    Map<String, Object> getModuleStatistics();

    /**
     * 获取文件类型分布统计
     *
     * @return 文件类型分布
     */
    Map<String, Object> getFileTypeStatistics();

    /**
     * 获取上传趋势统计
     *
     * @param days 统计天数
     * @return 上传趋势数据
     */
    Map<String, Object> getUploadTrend(int days);

    /**
     * 获取热门文件统计
     *
     * @param limit 限制数量
     * @return 热门文件列表
     */
    Map<String, Object> getHotFiles(int limit);
} 