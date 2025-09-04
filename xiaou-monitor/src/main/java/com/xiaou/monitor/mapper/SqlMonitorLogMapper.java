package com.xiaou.monitor.mapper;

import com.xiaou.monitor.domain.SqlMonitorLog;
import com.xiaou.monitor.domain.SqlStatistics;
import com.xiaou.monitor.dto.SqlMonitorQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SQL监控日志Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SqlMonitorLogMapper {

    /**
     * 插入单条监控日志
     */
    int insert(SqlMonitorLog log);

    /**
     * 批量插入监控日志
     */
    int batchInsert(@Param("logs") List<SqlMonitorLog> logs);

    /**
     * 根据条件查询监控日志
     */
    List<SqlMonitorLog> selectByCondition(@Param("request") SqlMonitorQueryRequest request);

    /**
     * 根据条件统计数量
     */
    long countByCondition(@Param("request") SqlMonitorQueryRequest request);

    /**
     * 查询SQL统计信息
     */
    List<SqlStatistics> selectStatistics(@Param("startDate") String startDate, 
                                        @Param("endDate") String endDate);

    /**
     * 查询SQL执行频次统计
     */
    List<SqlStatistics> selectFrequencyStatistics(@Param("date") String date, 
                                                  @Param("limit") Integer limit);

    /**
     * 删除过期数据
     */
    int deleteExpiredData(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 删除所有数据
     */
    int deleteAll();
} 