package io.github.xiaou61.ai.internal.telemetry;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 完整载荷表的读写（WORK-030 阶段 B）。
 *
 * <p>与 `ai_run_spans` 一样用 `run_trace_id` 关联。区别是这张表**有独立的保留期**：
 * 它是诊断用的临时证据，不是账目，30 天就够（U-20）。
 */
@Mapper
interface AiRunPayloadMapper {

    @Insert("""
            <script>
            INSERT INTO ai_run_payloads
                (run_trace_id, seq, request_body, response_body, request_bytes, response_bytes,
                 truncated, created_at)
            VALUES
            <foreach collection="rows" item="row" separator=",">
                (#{row.runTraceId}, #{row.seq}, #{row.requestBody}, #{row.responseBody},
                 #{row.requestBytes}, #{row.responseBytes}, #{row.truncated}, #{row.createdAt})
            </foreach>
            </script>
            """)
    int insertBatch(@Param("rows") List<Row> rows);

    @Select("""
            SELECT run_trace_id, seq, request_body, response_body, request_bytes, response_bytes, truncated
            FROM ai_run_payloads
            WHERE run_trace_id = #{traceId}
            ORDER BY seq
            """)
    List<Row> findByTraceId(@Param("traceId") String traceId);

    /** 按创建时间删过期行，一次一批。返回删除条数。 */
    @Delete("""
            DELETE FROM ai_run_payloads
            WHERE created_at < #{before}
            LIMIT #{limit}
            """)
    int deleteExpired(@Param("before") Instant before, @Param("limit") int limit);

    /** 注销级联：这个人所有轮次的载荷一起删（子查询走 trace_id）。 */
    @Delete("""
            DELETE FROM ai_run_payloads
            WHERE run_trace_id IN (SELECT trace_id FROM ai_runs WHERE user_id = #{userId})
            """)
    int deleteByUser(@Param("userId") long userId);

    /** 表行的可变载体（MyBatis 需要 setter）。 */
    class Row {
        private String runTraceId;
        private Integer seq;
        private String requestBody;
        private String responseBody;
        private Integer requestBytes;
        private Integer responseBytes;
        private Boolean truncated;
        private Instant createdAt;

        public String getRunTraceId() { return runTraceId; }
        public void setRunTraceId(String runTraceId) { this.runTraceId = runTraceId; }
        public Integer getSeq() { return seq; }
        public void setSeq(Integer seq) { this.seq = seq; }
        public String getRequestBody() { return requestBody; }
        public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
        public String getResponseBody() { return responseBody; }
        public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
        public Integer getRequestBytes() { return requestBytes; }
        public void setRequestBytes(Integer requestBytes) { this.requestBytes = requestBytes; }
        public Integer getResponseBytes() { return responseBytes; }
        public void setResponseBytes(Integer responseBytes) { this.responseBytes = responseBytes; }
        public Boolean getTruncated() { return truncated; }
        public void setTruncated(Boolean truncated) { this.truncated = truncated; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }
}
