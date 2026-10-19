package io.github.xiaou61.ai.internal.telemetry;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 统计聚合（WORK-022 T10）。
 *
 * <p><b>分位数在 SQL 里算</b>，不把明细行拉进应用内存：`ROW_NUMBER() OVER (ORDER BY ...)`
 * 的最近秩法与"排序后手算"一致（AC-010），而一张百万行表拉进 JVM 只是等着 OOM。
 *
 * <p>只读聚合，不返回任何正文——统计页要的是数字，不是内容。
 */
@Mapper
public interface TelemetrySummaryMapper {

    /** 按状态分组的计数。 */
    @Select("""
            SELECT status, COUNT(*) AS count FROM ai_runs
            WHERE started_at >= #{from} AND started_at < #{to}
            GROUP BY status
            """)
    List<CountBy> countByStatus(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            SELECT kind AS name, COUNT(*) AS count FROM ai_runs
            WHERE started_at >= #{from} AND started_at < #{to}
            GROUP BY kind ORDER BY count DESC
            """)
    List<CountBy> countByKind(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            SELECT COALESCE(model, '(unknown)') AS name, COUNT(*) AS count FROM ai_runs
            WHERE started_at >= #{from} AND started_at < #{to}
            GROUP BY model ORDER BY count DESC
            """)
    List<CountBy> countByModel(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            SELECT error_type AS name, COUNT(*) AS count FROM ai_runs
            WHERE started_at >= #{from} AND started_at < #{to} AND error_type IS NOT NULL
            GROUP BY error_type ORDER BY count DESC LIMIT 10
            """)
    List<CountBy> countByErrorType(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            SELECT name, COUNT(*) AS count FROM ai_run_spans
            WHERE kind = 'tool' AND started_at >= #{from} AND started_at < #{to}
            GROUP BY name ORDER BY count DESC LIMIT 10
            """)
    List<CountBy> countByTool(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * 耗时分位数（最近秩法）。
     *
     * <p>`duration_ms` 为 NULL 的行（仍在 running）不参与——把它们当 0 会把 p50 拉低到失真。
     */
    @Select("""
            WITH ranked AS (
              SELECT duration_ms,
                     ROW_NUMBER() OVER (ORDER BY duration_ms) AS rn,
                     COUNT(*) OVER () AS n
              FROM ai_runs
              WHERE started_at >= #{from} AND started_at < #{to}
                AND status IN ('ok', 'failed', 'timeout') AND duration_ms IS NOT NULL
            )
            SELECT MAX(CASE WHEN rn = CEIL(n * 0.50) THEN duration_ms END) AS p50,
                   MAX(CASE WHEN rn = CEIL(n * 0.95) THEN duration_ms END) AS p95
            FROM ranked
            """)
    Percentiles durationPercentiles(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            WITH ranked AS (
              SELECT ttft_ms,
                     ROW_NUMBER() OVER (ORDER BY ttft_ms) AS rn,
                     COUNT(*) OVER () AS n
              FROM ai_runs
              WHERE started_at >= #{from} AND started_at < #{to}
                AND status = 'ok' AND ttft_ms IS NOT NULL
            )
            SELECT MAX(CASE WHEN rn = CEIL(n * 0.50) THEN ttft_ms END) AS p50,
                   MAX(CASE WHEN rn = CEIL(n * 0.95) THEN ttft_ms END) AS p95
            FROM ranked
            """)
    Percentiles ttftPercentiles(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            SELECT COALESCE(SUM(input_tokens), 0) AS inputTokens,
                   COALESCE(SUM(output_tokens), 0) AS outputTokens,
                   COALESCE(SUM(credits), 0) AS credits
            FROM ai_runs WHERE started_at >= #{from} AND started_at < #{to}
            """)
    Totals totals(@Param("from") Instant from, @Param("to") Instant to);

    @Select("""
            SELECT kind AS name, COUNT(*) AS count FROM ai_runs
            WHERE started_at >= #{from} AND started_at < #{to} AND kind IS NOT NULL
            GROUP BY kind
            """)
    List<CountBy> distinctKinds(@Param("from") Instant from, @Param("to") Instant to);

    @Select("SELECT COALESCE(model, '(unknown)') AS name, COUNT(*) AS count FROM ai_runs "
            + "WHERE started_at >= #{from} AND started_at < #{to} AND model IS NOT NULL "
            + "GROUP BY model")
    List<CountBy> distinctModels(@Param("from") Instant from, @Param("to") Instant to);

    record CountBy(String name, long count) {
    }

    record Percentiles(Integer p50, Integer p95) {
    }

    record Totals(long inputTokens, long outputTokens, long credits) {
    }
}
