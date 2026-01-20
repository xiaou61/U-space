package com.xiaou.sqloptimizer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * SQL分析请求DTO
 *
 * @author xiaou
 */
@Data
public class SqlAnalyzeRequest {

    /**
     * 待优化的SQL语句
     */
    @NotBlank(message = "SQL语句不能为空")
    private String sql;

    /**
     * EXPLAIN执行计划结果
     */
    @NotBlank(message = "EXPLAIN结果不能为空")
    private String explainResult;

    /**
     * EXPLAIN格式（TABLE/JSON）
     */
    private String explainFormat = "TABLE";

    /**
     * 表结构列表
     */
    @NotBlank(message = "表结构不能为空")
    private List<TableStructure> tableStructures;

    /**
     * MySQL版本
     */
    private String mysqlVersion = "8.0";

    /**
     * 表结构
     */
    @Data
    public static class TableStructure {
        /**
         * 表名
         */
        private String tableName;

        /**
         * DDL语句
         */
        private String ddl;
    }
}
