package com.xiaou.ai.service;

import com.xiaou.ai.dto.sql.SqlAnalyzeResult;

/**
 * SQL优化AI服务接口
 *
 * @author xiaou
 */
public interface AiSqlOptimizeService {

    /**
     * 分析SQL并给出优化建议
     *
     * @param sql             待优化的SQL语句
     * @param explainResult   EXPLAIN执行计划结果
     * @param explainFormat   EXPLAIN格式（TABLE/JSON）
     * @param tableStructures 表结构DDL（JSON数组格式）
     * @param mysqlVersion    MySQL版本
     * @return 分析结果
     */
    SqlAnalyzeResult analyzeSql(String sql, String explainResult, String explainFormat,
                                 String tableStructures, String mysqlVersion);
}
