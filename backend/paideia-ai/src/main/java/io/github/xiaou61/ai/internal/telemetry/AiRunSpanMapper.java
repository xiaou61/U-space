package io.github.xiaou61.ai.internal.telemetry;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 步骤表的读写（WORK-022 T01）。
 *
 * <p>用 `run_trace_id` 关联而不是自增 run_id：异步写入下拿不到刚插入的 id，
 * 而两次写（UPDATE 轮次 + INSERT 步骤）互相独立、任一次失败不牵连另一次。
 *
 * <p>**批量插入**：一轮最多 5 条步骤，逐条 insert 会多 4 次往返；批量在写线程里做，
 * 不影响用户。
 */
@Mapper
public interface AiRunSpanMapper {

    @Insert("""
            <script>
            INSERT INTO ai_run_spans
                (run_trace_id, parent_seq, seq, kind, name, status, error_type,
                 started_at, duration_ms, ttft_ms, input_tokens, output_tokens,
                 cache_read_tokens, attrs)
            VALUES
            <foreach collection="rows" item="row" separator=",">
                (#{row.runTraceId}, #{row.parentSeq}, #{row.seq}, #{row.kind}, #{row.name},
                 #{row.status}, #{row.errorType}, #{row.startedAt}, #{row.durationMs},
                 #{row.ttftMs}, #{row.inputTokens}, #{row.outputTokens},
                 #{row.cacheReadTokens}, #{row.attrsJson})
            </foreach>
            </script>
            """)
    int insertBatch(@Param("rows") List<AiRunSpanRow> rows);

    @Select("""
            SELECT id, run_trace_id, parent_seq, seq, kind, name, status, error_type,
                   started_at, duration_ms, ttft_ms, input_tokens, output_tokens,
                   cache_read_tokens, attrs AS attrsJson
            FROM ai_run_spans
            WHERE run_trace_id = #{traceId}
            ORDER BY seq
            """)
    List<AiRunSpanRow> findByTraceId(@Param("traceId") String traceId);

    @org.apache.ibatis.annotations.Delete("""
            DELETE FROM ai_run_spans
            WHERE run_trace_id IN (
                SELECT trace_id FROM ai_runs WHERE user_id = #{userId}
            )
            """)
    int deleteByUserRuns(@Param("userId") long userId);

    @org.apache.ibatis.annotations.Delete("""
            <script>
            DELETE FROM ai_run_spans WHERE run_trace_id IN
            <foreach collection="traceIds" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    int deleteByTraceIds(@Param("traceIds") List<String> traceIds);

    /** 一行的视图。attrs 用 attrsJson 承接（列名与字段名对不上，必须显式起别名）。 */
    class AiRunSpanRow {
        private Long id;
        private String runTraceId;
        private Integer parentSeq;
        private Integer seq;
        private String kind;
        private String name;
        private String status;
        private String errorType;
        private Instant startedAt;
        private Integer durationMs;
        private Integer ttftMs;
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer cacheReadTokens;
        private String attrsJson;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getRunTraceId() { return runTraceId; }
        public void setRunTraceId(String runTraceId) { this.runTraceId = runTraceId; }
        public Integer getParentSeq() { return parentSeq; }
        public void setParentSeq(Integer parentSeq) { this.parentSeq = parentSeq; }
        public Integer getSeq() { return seq; }
        public void setSeq(Integer seq) { this.seq = seq; }
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        public Instant getStartedAt() { return startedAt; }
        public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
        public Integer getDurationMs() { return durationMs; }
        public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
        public Integer getTtftMs() { return ttftMs; }
        public void setTtftMs(Integer ttftMs) { this.ttftMs = ttftMs; }
        public Integer getInputTokens() { return inputTokens; }
        public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
        public Integer getOutputTokens() { return outputTokens; }
        public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
        public Integer getCacheReadTokens() { return cacheReadTokens; }
        public void setCacheReadTokens(Integer cacheReadTokens) { this.cacheReadTokens = cacheReadTokens; }
        public String getAttrsJson() { return attrsJson; }
        public void setAttrsJson(String attrsJson) { this.attrsJson = attrsJson; }
    }
}
