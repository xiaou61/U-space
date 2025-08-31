package com.xiaou.monitor.controller;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.monitor.domain.SqlMonitorLog;
import com.xiaou.monitor.domain.SqlStatistics;
import com.xiaou.monitor.dto.SqlMonitorQueryRequest;
import com.xiaou.monitor.service.SqlMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SQL监控控制器
 *
 * @author xiaou
 */
@Tag(name = "SQL监控管理", description = "SQL执行监控相关接口")
@RestController
@RequestMapping("/monitor/sql")
@RequiredArgsConstructor
public class SqlMonitorController {

    private final SqlMonitorService sqlMonitorService;

    @Operation(summary = "分页查询SQL监控日志")
    @PostMapping("/logs")
    @RequireAdmin
    public Result<PageResult<SqlMonitorLog>> queryLogs(@RequestBody SqlMonitorQueryRequest request) {
        PageResult<SqlMonitorLog> result = sqlMonitorService.queryMonitorLogs(request);
        return Result.ok(result);
    }

    @Operation(summary = "查询慢SQL列表")
    @PostMapping("/slow-sql")
    @RequireAdmin
    public Result<PageResult<SqlMonitorLog>> querySlowSql(@RequestBody SqlMonitorQueryRequest request) {
        PageResult<SqlMonitorLog> result = sqlMonitorService.querySlowSqlLogs(request);
        return Result.ok(result);
    }

    @Operation(summary = "查询SQL统计信息")
    @GetMapping("/statistics")
    @RequireAdmin
    public Result<List<SqlStatistics>> queryStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<SqlStatistics> result = sqlMonitorService.queryStatistics(startDate, endDate);
        return Result.ok(result);
    }

    @Operation(summary = "查询SQL执行频次统计")
    @GetMapping("/frequency")
    @RequireAdmin
    public Result<List<SqlStatistics>> queryFrequencyStatistics(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<SqlStatistics> result = sqlMonitorService.queryFrequencyStatistics(date, limit);
        return Result.ok(result);
    }

    @Operation(summary = "查询实时监控数据")
    @GetMapping("/realtime")
    @RequireAdmin
    public Result<List<SqlMonitorLog>> queryRealtimeData(
            @RequestParam(required = false) String traceId) {
        List<SqlMonitorLog> result = sqlMonitorService.queryRealtimeData(traceId);
        return Result.ok(result);
    }

    @Operation(summary = "清理过期数据")
    @DeleteMapping("/expired")
    @RequireAdmin
    public Result<Void> deleteExpiredData(@RequestParam(defaultValue = "30") Integer retentionDays) {
        sqlMonitorService.deleteExpiredData(retentionDays);
        return Result.ok();
    }

    @Operation(summary = "清空所有监控数据")
    @DeleteMapping("/all")
    @RequireAdmin
    public Result<Void> clearAllData() {
        sqlMonitorService.clearAllData();
        return Result.ok();
    }
} 