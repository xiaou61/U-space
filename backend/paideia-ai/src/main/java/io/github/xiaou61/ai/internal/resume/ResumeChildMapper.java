package io.github.xiaou61.ai.internal.resume;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 简历相关三张子表的读写（WORK-018）。
 *
 * <p>三张表放进同一个 Mapper 而不是各开一个：它们的生活史完全绑在主简历上（删主简历就全删），
 * 分开只会多两个文件与两次装配，换不来任何独立演化的可能。
 *
 * <p>**每条 SQL 都带 `user_id`**，即使已经通过主简历 id 关联过了——那是刻意的冗余：
 * 子表里存了 `user_id`，就能在不 join 主表的情况下做归属校验，少一次 join 也就少一次
 * "忘了带条件"的机会。
 */
@Mapper
interface ResumeChildMapper {

    // ── 编辑快照 ────────────────────────────────────────────────────

    @Insert("""
            INSERT INTO resume_edit_history (document_id, user_id, data_json, created_at)
            VALUES (#{documentId}, #{userId}, #{dataJson}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertHistory(HistoryRow row);

    @Select("""
            SELECT id, document_id, user_id, data_json, created_at
            FROM resume_edit_history
            WHERE document_id = #{documentId} AND user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<HistoryRow> listHistory(@Param("documentId") long documentId,
                                 @Param("userId") long userId, @Param("limit") int limit);

    /** 最近一条快照（用于 60 秒合并窗口）。 */
    @Select("""
            SELECT id, document_id, user_id, data_json, created_at
            FROM resume_edit_history
            WHERE document_id = #{documentId} AND user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
    HistoryRow findLatestHistory(@Param("documentId") long documentId, @Param("userId") long userId);

    @Select("""
            SELECT id, document_id, user_id, data_json, created_at
            FROM resume_edit_history
            WHERE id = #{id} AND user_id = #{userId}
            """)
    HistoryRow findHistory(@Param("id") long id, @Param("userId") long userId);

    /** 合并窗口内覆盖同一快照。 */
    @Update("""
            UPDATE resume_edit_history SET data_json = #{dataJson}, created_at = #{createdAt}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int updateHistory(HistoryRow row);

    /**
     * 裁剪：删掉第 {@code keep} 条之后的所有快照。
     *
     * <p>用 `id NOT IN (子查询)` 而不是按时间截断：同一毫秒内可能有多条（并发保存），
     * 按时间切会把边界上的几条一起删掉或一起留下。
     */
    @Delete("""
            DELETE FROM resume_edit_history
            WHERE document_id = #{documentId} AND user_id = #{userId}
              AND id NOT IN (
                SELECT id FROM (
                  SELECT id FROM resume_edit_history
                  WHERE document_id = #{documentId} AND user_id = #{userId}
                  ORDER BY created_at DESC, id DESC
                  LIMIT #{keep}
                ) keep_rows
              )
            """)
    int trimHistory(@Param("documentId") long documentId, @Param("userId") long userId,
                    @Param("keep") int keep);

    // ── 岗位版本 ────────────────────────────────────────────────────

    @Insert("""
            INSERT INTO resume_versions
                (document_id, user_id, company, role, jd_text, status, analysis_json, final_json,
                 created_at, updated_at)
            VALUES (#{documentId}, #{userId}, #{company}, #{role}, #{jdText}, #{status},
                    #{analysisJson}, #{finalJson}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertVersion(VersionRow row);

    @Select("""
            SELECT id, document_id, user_id, company, role, jd_text, status, analysis_json,
                   final_json, created_at, updated_at
            FROM resume_versions
            WHERE document_id = #{documentId} AND user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            """)
    List<VersionRow> listVersions(@Param("documentId") long documentId, @Param("userId") long userId);

    @Select("""
            SELECT id, document_id, user_id, company, role, jd_text, status, analysis_json,
                   final_json, created_at, updated_at
            FROM resume_versions
            WHERE id = #{id} AND user_id = #{userId}
            """)
    VersionRow findVersion(@Param("id") long id, @Param("userId") long userId);

    @Update("""
            UPDATE resume_versions
            SET analysis_json = #{analysisJson}, updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId} AND status = 'draft'
            """)
    int updateAnalysis(@Param("id") long id, @Param("userId") long userId,
                       @Param("analysisJson") String analysisJson, @Param("updatedAt") Instant updatedAt);

    /** 定稿：**只有 draft 能被定稿**，重复调用不会把已定稿的内容再改一次。 */
    @Update("""
            UPDATE resume_versions
            SET status = 'finalized', final_json = #{finalJson}, updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId} AND status = 'draft'
            """)
    int finalize(@Param("id") long id, @Param("userId") long userId,
                 @Param("finalJson") String finalJson, @Param("updatedAt") Instant updatedAt);

    @Delete("DELETE FROM resume_versions WHERE id = #{id} AND user_id = #{userId}")
    int deleteVersion(@Param("id") long id, @Param("userId") long userId);

    /** 删一份主简历下的全部版本（删主简历时用）。 */
    @Delete("DELETE FROM resume_versions WHERE document_id = #{documentId} AND user_id = #{userId}")
    int deleteVersionsOfDocument(@Param("documentId") long documentId, @Param("userId") long userId);

    /** 删一份主简历下的全部快照（删主简历时用）。 */
    @Delete("DELETE FROM resume_edit_history WHERE document_id = #{documentId} AND user_id = #{userId}")
    int deleteHistoryOfDocument(@Param("documentId") long documentId, @Param("userId") long userId);

    /** 删一个版本下的全部提案（删版本或重新生成时用）。 */
    @Delete("DELETE FROM resume_proposals WHERE user_id = #{userId} AND version_id IN"
            + " (SELECT id FROM resume_versions WHERE document_id = #{documentId})")
    int deleteProposalsOfDocument(@Param("documentId") long documentId, @Param("userId") long userId);

    // ── 提案 ────────────────────────────────────────────────────────

    @Insert("""
            INSERT INTO resume_proposals
                (version_id, user_id, seq, entry_ref, field, original_text, proposed_text,
                 change_type, rationale, covers_keywords, accepted, gate_status, gate_violations,
                 created_at, updated_at)
            VALUES (#{versionId}, #{userId}, #{seq}, #{entryRef}, #{field}, #{originalText},
                    #{proposedText}, #{changeType}, #{rationale}, #{coversKeywords}, #{accepted},
                    #{gateStatus}, #{gateViolations}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProposal(ProposalRow row);

    @Select("""
            SELECT id, version_id, user_id, seq, entry_ref, field, original_text, proposed_text,
                   change_type, rationale, covers_keywords, accepted, gate_status, gate_violations,
                   created_at, updated_at
            FROM resume_proposals
            WHERE version_id = #{versionId} AND user_id = #{userId}
            ORDER BY seq ASC, id ASC
            """)
    List<ProposalRow> listProposals(@Param("versionId") long versionId, @Param("userId") long userId);

    @Select("""
            SELECT id, version_id, user_id, seq, entry_ref, field, original_text, proposed_text,
                   change_type, rationale, covers_keywords, accepted, gate_status, gate_violations,
                   created_at, updated_at
            FROM resume_proposals
            WHERE id = #{id} AND user_id = #{userId}
            """)
    ProposalRow findProposal(@Param("id") long id, @Param("userId") long userId);

    @Update("""
            UPDATE resume_proposals
            SET accepted = #{accepted}, proposed_text = #{proposedText},
                gate_status = #{gateStatus}, gate_violations = #{gateViolations},
                updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int updateProposal(ProposalRow row);

    /** 重新生成时先清空旧提案（同一个版本里只保留一批）。 */
    @Delete("DELETE FROM resume_proposals WHERE version_id = #{versionId} AND user_id = #{userId}")
    int deleteProposals(@Param("versionId") long versionId, @Param("userId") long userId);

    // ── 行类型 ──────────────────────────────────────────────────────

    class HistoryRow {
        private Long id;
        private Long documentId;
        private Long userId;
        private String dataJson;
        private Instant createdAt;

        Long getId() {
            return id;
        }

        void setId(Long id) {
            this.id = id;
        }

        Long getDocumentId() {
            return documentId;
        }

        void setDocumentId(Long documentId) {
            this.documentId = documentId;
        }

        Long getUserId() {
            return userId;
        }

        void setUserId(Long userId) {
            this.userId = userId;
        }

        String getDataJson() {
            return dataJson;
        }

        void setDataJson(String dataJson) {
            this.dataJson = dataJson;
        }

        Instant getCreatedAt() {
            return createdAt;
        }

        void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

    class VersionRow {
        private Long id;
        private Long documentId;
        private Long userId;
        private String company;
        private String role;
        private String jdText;
        private String status;
        private String analysisJson;
        private String finalJson;
        private Instant createdAt;
        private Instant updatedAt;

        Long getId() {
            return id;
        }

        void setId(Long id) {
            this.id = id;
        }

        Long getDocumentId() {
            return documentId;
        }

        void setDocumentId(Long documentId) {
            this.documentId = documentId;
        }

        Long getUserId() {
            return userId;
        }

        void setUserId(Long userId) {
            this.userId = userId;
        }

        String getCompany() {
            return company;
        }

        void setCompany(String company) {
            this.company = company;
        }

        String getRole() {
            return role;
        }

        void setRole(String role) {
            this.role = role;
        }

        String getJdText() {
            return jdText;
        }

        void setJdText(String jdText) {
            this.jdText = jdText;
        }

        String getStatus() {
            return status;
        }

        void setStatus(String status) {
            this.status = status;
        }

        String getAnalysisJson() {
            return analysisJson;
        }

        void setAnalysisJson(String analysisJson) {
            this.analysisJson = analysisJson;
        }

        String getFinalJson() {
            return finalJson;
        }

        void setFinalJson(String finalJson) {
            this.finalJson = finalJson;
        }

        Instant getCreatedAt() {
            return createdAt;
        }

        void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        Instant getUpdatedAt() {
            return updatedAt;
        }

        void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    class ProposalRow {
        private Long id;
        private Long versionId;
        private Long userId;
        private int seq;
        private String entryRef;
        private String field;
        private String originalText;
        private String proposedText;
        private String changeType;
        private String rationale;
        private String coversKeywords;
        private boolean accepted;
        private String gateStatus;
        private String gateViolations;
        private Instant createdAt;
        private Instant updatedAt;

        Long getId() {
            return id;
        }

        void setId(Long id) {
            this.id = id;
        }

        Long getVersionId() {
            return versionId;
        }

        void setVersionId(Long versionId) {
            this.versionId = versionId;
        }

        Long getUserId() {
            return userId;
        }

        void setUserId(Long userId) {
            this.userId = userId;
        }

        int getSeq() {
            return seq;
        }

        void setSeq(int seq) {
            this.seq = seq;
        }

        String getEntryRef() {
            return entryRef;
        }

        void setEntryRef(String entryRef) {
            this.entryRef = entryRef;
        }

        String getField() {
            return field;
        }

        void setField(String field) {
            this.field = field;
        }

        String getOriginalText() {
            return originalText;
        }

        void setOriginalText(String originalText) {
            this.originalText = originalText;
        }

        String getProposedText() {
            return proposedText;
        }

        void setProposedText(String proposedText) {
            this.proposedText = proposedText;
        }

        String getChangeType() {
            return changeType;
        }

        void setChangeType(String changeType) {
            this.changeType = changeType;
        }

        String getRationale() {
            return rationale;
        }

        void setRationale(String rationale) {
            this.rationale = rationale;
        }

        String getCoversKeywords() {
            return coversKeywords;
        }

        void setCoversKeywords(String coversKeywords) {
            this.coversKeywords = coversKeywords;
        }

        boolean isAccepted() {
            return accepted;
        }

        void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }

        String getGateStatus() {
            return gateStatus;
        }

        void setGateStatus(String gateStatus) {
            this.gateStatus = gateStatus;
        }

        String getGateViolations() {
            return gateViolations;
        }

        void setGateViolations(String gateViolations) {
            this.gateViolations = gateViolations;
        }

        Instant getCreatedAt() {
            return createdAt;
        }

        void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        Instant getUpdatedAt() {
            return updatedAt;
        }

        void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
