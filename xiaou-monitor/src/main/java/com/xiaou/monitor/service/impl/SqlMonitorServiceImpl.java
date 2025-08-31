package com.xiaou.monitor.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.RedisUtil;
import com.xiaou.monitor.domain.SqlMonitorLog;
import com.xiaou.monitor.domain.SqlStatistics;
import com.xiaou.monitor.dto.SqlMonitorQueryRequest;
import com.xiaou.monitor.mapper.SqlMonitorLogMapper;
import com.xiaou.monitor.service.SqlMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * SQL监控服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlMonitorServiceImpl implements SqlMonitorService {

    private final SqlMonitorLogMapper sqlMonitorLogMapper;
    private final RedisUtil redisUtil;

    /**
     * Redis缓存Key前缀
     */
    private static final String REDIS_KEY_PREFIX = "sql:monitor:";
    private static final String REALTIME_KEY = REDIS_KEY_PREFIX + "realtime:";
    private static final String STATISTICS_KEY = REDIS_KEY_PREFIX + "statistics:";

    /**
     * 内存队列，用于异步批量处理
     */
    private final Queue<SqlMonitorLog> pendingLogs = new ConcurrentLinkedQueue<>();

    /**
     * 批处理阈值
     */
    private static final int BATCH_SIZE = 50;

    @Override
    @Async("monitorTaskExecutor")
    public void saveMonitorLogAsync(SqlMonitorLog monitorLog) {
        try {
            // 加入待处理队列
            pendingLogs.offer(monitorLog);
            
            // 实时数据缓存到Redis (保留最近100条)
            cacheRealtimeData(monitorLog);
            
            // 更新统计信息到Redis
            updateStatisticsCache(monitorLog);
            
            // 如果队列达到批处理阈值，立即处理
            if (pendingLogs.size() >= BATCH_SIZE) {
                processPendingLogs();
            }
            
        } catch (Exception e) {
            log.error("异步保存SQL监控日志失败", e);
        }
    }

    @Override
    public void batchSaveMonitorLogs(List<SqlMonitorLog> monitorLogs) {
        if (monitorLogs == null || monitorLogs.isEmpty()) {
            return;
        }
        
        try {
            sqlMonitorLogMapper.batchInsert(monitorLogs);
            log.debug("批量保存SQL监控日志成功，数量: {}", monitorLogs.size());
        } catch (Exception e) {
            log.error("批量保存SQL监控日志失败", e);
        }
    }

    @Override
    public PageResult<SqlMonitorLog> queryMonitorLogs(SqlMonitorQueryRequest request) {
        try {
            // 查询总数
            long total = sqlMonitorLogMapper.countByCondition(request);
            if (total == 0) {
                return new PageResult<>(Collections.emptyList(), total, request.getPageNum(), request.getPageSize());
            }
            
            // 计算分页参数
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            
            // 查询数据
            List<SqlMonitorLog> records = sqlMonitorLogMapper.selectByCondition(request, offset, request.getPageSize());
            
            return new PageResult<>(records, total, request.getPageNum(), request.getPageSize());
            
        } catch (Exception e) {
            log.error("查询SQL监控日志失败", e);
            return new PageResult<>(Collections.emptyList(), 0L, request.getPageNum(), request.getPageSize());
        }
    }

    @Override
    public PageResult<SqlMonitorLog> querySlowSqlLogs(SqlMonitorQueryRequest request) {
        request.setSlowSqlOnly(true);
        return queryMonitorLogs(request);
    }

    @Override
    public List<SqlStatistics> queryStatistics(String startDate, String endDate) {
        try {
            return sqlMonitorLogMapper.selectStatistics(startDate, endDate);
        } catch (Exception e) {
            log.error("查询SQL统计信息失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<SqlStatistics> queryFrequencyStatistics(String date, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 20;
            }
            return sqlMonitorLogMapper.selectFrequencyStatistics(date, limit);
        } catch (Exception e) {
            log.error("查询SQL执行频次统计失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<SqlMonitorLog> queryRealtimeData(String traceId) {
        try {
            if (StrUtil.isNotBlank(traceId)) {
                // 根据跟踪ID查询实时数据
                String cacheKey = REALTIME_KEY + traceId;
                List<SqlMonitorLog> cachedData = redisUtil.get(cacheKey);
                if (cachedData != null) {
                    return cachedData;
                }
            }
            
            // 从Redis获取最近的实时数据
            String realtimeKey = REALTIME_KEY + "latest";
            List<SqlMonitorLog> realtimeData = redisUtil.get(realtimeKey);
            return realtimeData != null ? realtimeData : Collections.emptyList();
            
        } catch (Exception e) {
            log.error("查询实时SQL监控数据失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteExpiredData(int retentionDays) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            int deletedCount = sqlMonitorLogMapper.deleteExpiredData(expireTime);
            log.info("删除过期SQL监控数据完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("删除过期SQL监控数据失败", e);
        }
    }

    @Override
    public void clearAllData() {
        try {
            sqlMonitorLogMapper.deleteAll();
            // 清除Redis缓存
            redisUtil.deleteByPattern(REDIS_KEY_PREFIX + "*");
            log.info("清空SQL监控数据完成");
        } catch (Exception e) {
            log.error("清空SQL监控数据失败", e);
        }
    }

    /**
     * 缓存实时数据到Redis
     */
    private void cacheRealtimeData(SqlMonitorLog monitorLog) {
        try {
            // 按跟踪ID缓存
            if (StrUtil.isNotBlank(monitorLog.getTraceId())) {
                String traceKey = REALTIME_KEY + monitorLog.getTraceId();
                List<SqlMonitorLog> traceLogs = redisUtil.get(traceKey);
                if (traceLogs == null) {
                    traceLogs = new ArrayList<>();
                }
                traceLogs.add(monitorLog);
                redisUtil.set(traceKey, traceLogs, 300); // 5分钟过期
            }
            
            // 缓存最新数据
            String latestKey = REALTIME_KEY + "latest";
            List<SqlMonitorLog> latestLogs = redisUtil.get(latestKey);
            if (latestLogs == null) {
                latestLogs = new LinkedList<>();
            }
            
            latestLogs.add(0, monitorLog); // 添加到头部
            
            // 保持最新100条
            if (latestLogs.size() > 100) {
                latestLogs = latestLogs.subList(0, 100);
            }
            
            redisUtil.set(latestKey, latestLogs, 600); // 10分钟过期
            
        } catch (Exception e) {
            log.warn("缓存实时数据失败", e);
        }
    }

    /**
     * 更新统计信息到Redis
     */
    private void updateStatisticsCache(SqlMonitorLog monitorLog) {
        try {
            String date = DateUtil.format(Date.from(monitorLog.getExecuteTime().atZone(java.time.ZoneId.systemDefault()).toInstant()), "yyyy-MM-dd");
            String statsKey = STATISTICS_KEY + date;
            
            // 这里可以实现更复杂的统计逻辑
            // 为了简单起见，暂时只记录基本信息
            Map<String, Object> stats = redisUtil.get(statsKey);
            if (stats == null) {
                stats = new HashMap<>();
            }
            
            // 更新基本统计
            stats.put("totalCount", ((Integer) stats.getOrDefault("totalCount", 0)) + 1);
            if (Boolean.TRUE.equals(monitorLog.getSlowSql())) {
                stats.put("slowSqlCount", ((Integer) stats.getOrDefault("slowSqlCount", 0)) + 1);
            }
            if (Boolean.FALSE.equals(monitorLog.getSuccess())) {
                stats.put("errorCount", ((Integer) stats.getOrDefault("errorCount", 0)) + 1);
            }
            
            redisUtil.set(statsKey, stats, 86400); // 24小时过期
            
        } catch (Exception e) {
            log.warn("更新统计缓存失败", e);
        }
    }

    /**
     * 定时处理待处理日志（每5秒执行一次）
     */
    @Scheduled(fixedRate = 5000)
    public void processPendingLogs() {
        if (pendingLogs.isEmpty()) {
            return;
        }
        
        List<SqlMonitorLog> logsToProcess = new ArrayList<>();
        
        // 从队列中取出待处理的日志
        int count = 0;
        while (!pendingLogs.isEmpty() && count < BATCH_SIZE) {
            SqlMonitorLog log = pendingLogs.poll();
            if (log != null) {
                logsToProcess.add(log);
                count++;
            }
        }
        
        if (!logsToProcess.isEmpty()) {
            batchSaveMonitorLogs(logsToProcess);
        }
    }

    /**
     * 定时清理过期数据（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredData() {
        log.info("开始清理过期SQL监控数据");
        deleteExpiredData(30); // 保留30天数据
    }
} 