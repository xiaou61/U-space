package com.xiaou.sqloptimizer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 慢SQL优化分析记录实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class SqlOptimizeRecord {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 原始SQL
     */
    private String originalSql;

    /**
     * EXPLAIN结果
     */
    private String explainResult;

    /**
     * EXPLAIN格式（TABLE/JSON）
     */
    private String explainFormat;

    /**
     * 表结构JSON
     */
    private String tableStructures;

    /**
     * MySQL版本
     */
    private String mysqlVersion;

    /**
     * 分析结果JSON
     */
    private String analysisResult;

    /**
     * 性能评分
     */
    private Integer score;

    /**
     * 是否收藏
     */
    private Integer isFavorite;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Integer deleted;
}
