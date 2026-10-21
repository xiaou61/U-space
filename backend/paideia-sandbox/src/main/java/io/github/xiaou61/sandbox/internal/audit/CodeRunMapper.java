package io.github.xiaou61.sandbox.internal.audit;

import java.time.Instant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 运行审计的读写（WORK-015）。
 *
 * <p><b>不存源码</b>：只有 `code_hash` 与 `code_length`。审计回答的是"谁在什么时候跑了什么语言、
 * 结果如何、占了多少资源"，不是"他写了什么"。
 */
@Mapper
public interface CodeRunMapper {

    @Insert("""
            INSERT INTO code_runs
              (user_id, entry_id, case_id, language, status, exit_code,
               duration_ms, output_bytes, truncated, code_hash, code_length, created_at)
            VALUES
              (#{userId}, #{entryId}, #{caseId}, #{language}, #{status}, #{exitCode},
               #{durationMs}, #{outputBytes}, #{truncated}, #{codeHash}, #{codeLength}, #{createdAt})
            """)
    int insert(@Param("userId") long userId,
               @Param("entryId") Long entryId,
               @Param("caseId") String caseId,
               @Param("language") String language,
               @Param("status") String status,
               @Param("exitCode") Integer exitCode,
               @Param("durationMs") long durationMs,
               @Param("outputBytes") int outputBytes,
               @Param("truncated") boolean truncated,
               @Param("codeHash") String codeHash,
               @Param("codeLength") int codeLength,
               @Param("createdAt") Instant createdAt);

    /**
     * 当天已用次数。
     *
     * <p>**排除 `rejected`**：被配额或并发拒绝的那次没有真正执行，算进去会让"重试"变成惩罚——
     * 用户点满 50 次之后每隔一次就被永久挡在门外。
     */
    @Select("""
            SELECT COUNT(*) FROM code_runs
            WHERE user_id = #{userId}
              AND created_at >= #{since}
              AND status <> 'rejected'
              AND status <> 'disabled'
            """)
    int countSince(@Param("userId") long userId, @Param("since") Instant since);

    /**
     * 当天某语言的已用次数（WORK-032）。
     *
     * <p>SQL 的日配额单独一档（20 次），与 Java/Python 的 50 次分开算：一次 SQL 运行要起
     * 一个容器，成本是其它语言的数倍。`language` 列本来就有，不需要迁移。
     */
    @Select("""
            SELECT COUNT(*) FROM code_runs
            WHERE user_id = #{userId}
              AND created_at >= #{since}
              AND status <> 'rejected'
              AND status <> 'disabled'
              AND language = #{language}
            """)
    int countSinceForLanguage(@Param("userId") long userId, @Param("since") Instant since,
                              @Param("language") String language);

    /** 注销级联用。 */
    @org.apache.ibatis.annotations.Delete("DELETE FROM code_runs WHERE user_id = #{userId}")
    int deleteByUser(@Param("userId") long userId);
}
