package com.xiaou.monitor.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.monitor.domain.SqlMonitorLog;
import com.xiaou.monitor.domain.SqlStatistics;
import com.xiaou.monitor.dto.SqlMonitorQueryRequest;

import java.util.List;

/**
 * SQL监控服务接口
 *
 * @author xiaou
 */
public interface SqlMonitorService {

    /**
     * 异步保存监控日志
     */
    void saveMonitorLogAsync(SqlMonitorLog monitorLog);

    /**
     * 批量保存监控日志
     */
    void batchSaveMonitorLogs(List<SqlMonitorLog> monitorLogs);

    /**
     * 分页查询监控日志
     */
    PageResult<SqlMonitorLog> queryMonitorLogs(SqlMonitorQueryRequest request);

    /**
     * 查询慢SQL列表
     */
    PageResult<SqlMonitorLog> querySlowSqlLogs(SqlMonitorQueryRequest request);

    /**
     * 查询SQL统计信息
     */
    List<SqlStatistics> queryStatistics(String startDate, String endDate);

    /**
     * 查询SQL执行频次统计
     */
    List<SqlStatistics> queryFrequencyStatistics(String date, Integer limit);

    /**
     * 查询实时SQL监控数据
     */
    List<SqlMonitorLog> queryRealtimeData(String traceId);

    /**
     * 删除过期监控数据
     */
    void deleteExpiredData(int retentionDays);

    /**
     * 清空监控数据
     */
    void clearAllData();

    /**
     * 处理待处理的监控日志（定时任务）
     */
    void processPendingLogs();

    /**
     * 清理过期数据（定时任务）
     */
    void cleanupExpiredData();
} 