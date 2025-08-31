package com.xiaou.monitor.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * SQL统计信息
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class SqlStatistics {

    /**
     * SQL语句(简化后的模板)
     */
    private String sqlTemplate;

    /**
     * 执行次数
     */
    private Long executionCount;

    /**
     * 总执行时间
     */
    private Long totalExecutionTime;

    /**
     * 平均执行时间
     */
    private Double avgExecutionTime;

    /**
     * 最大执行时间
     */
    private Long maxExecutionTime;

    /**
     * 最小执行时间
     */
    private Long minExecutionTime;

    /**
     * 慢SQL次数
     */
    private Long slowSqlCount;

    /**
     * 错误次数
     */
    private Long errorCount;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 统计日期
     */
    private String statisticsDate;
} 