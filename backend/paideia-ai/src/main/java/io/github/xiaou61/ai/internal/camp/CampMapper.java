package io.github.xiaou61.ai.internal.camp;

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
 * 训练营五张表的读写（WORK-029）。
 *
 * <p><b>归属写在每一条 SQL 里</b>：凡是读用户数据的地方都带 {@code user_id}，漏写一处就等于
 * 把别人的营读出去。这一条比"服务层校验一次"更可靠——服务层漏一次，SQL 层还能兜住。
 *
 * <p>取不到一律由服务层转成 404，**不区分**"不存在"与"不是你的"：区分开就等于泄露
 * "某个营存在"。
 */
@Mapper
public interface CampMapper {

    // ── 营 ──────────────────────────────────────────────────────────

    @Insert("""
            INSERT INTO camp_programs
                (user_id, resume_document_id, resume_title, days, item_count, dropped_count,
                 diagnostic_submitted_at, retest_submitted_at, active_plan_id, created_at, updated_at)
            VALUES (#{userId}, #{resumeDocumentId}, #{resumeTitle}, #{days}, #{itemCount}, #{droppedCount},
                    NULL, NULL, NULL, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProgram(ProgramRow row);

    @Select("""
            SELECT id, user_id, resume_document_id, resume_title, days, item_count, dropped_count,
                   diagnostic_submitted_at, retest_submitted_at, active_plan_id, created_at, updated_at
            FROM camp_programs
            WHERE id = #{id} AND user_id = #{userId}
            """)
    ProgramRow findProgram(@Param("id") long id, @Param("userId") long userId);

    @Select("""
            SELECT id, user_id, resume_document_id, resume_title, days, item_count, dropped_count,
                   diagnostic_submitted_at, retest_submitted_at, active_plan_id, created_at, updated_at
            FROM camp_programs
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<ProgramRow> listPrograms(@Param("userId") long userId, @Param("limit") int limit);

    /** 进行中的营数（用于"最多 3 个"）。这里把"未删即进行中"作为口径。 */
    @Select("SELECT COUNT(*) FROM camp_programs WHERE user_id = #{userId}")
    int countPrograms(@Param("userId") long userId);

    @Update("""
            UPDATE camp_programs
            SET diagnostic_submitted_at = #{submittedAt}, updated_at = #{submittedAt}
            WHERE id = #{id} AND user_id = #{userId} AND diagnostic_submitted_at IS NULL
            """)
    int markDiagnosticSubmitted(@Param("id") long id, @Param("userId") long userId,
                                @Param("submittedAt") Instant submittedAt);

    @Update("""
            UPDATE camp_programs
            SET retest_submitted_at = #{submittedAt}, updated_at = #{submittedAt}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int markRetestSubmitted(@Param("id") long id, @Param("userId") long userId,
                            @Param("submittedAt") Instant submittedAt);

    @Update("""
            UPDATE camp_programs SET active_plan_id = #{planId}, updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int setActivePlan(@Param("id") long id, @Param("userId") long userId,
                      @Param("planId") long planId, @Param("updatedAt") Instant updatedAt);

    @Delete("DELETE FROM camp_programs WHERE id = #{id} AND user_id = #{userId}")
    int deleteProgram(@Param("id") long id, @Param("userId") long userId);

    // ── 题目 ────────────────────────────────────────────────────────

    @Insert("""
            INSERT INTO camp_items
                (program_id, user_id, form, seq, topic, concept_id, difficulty, stem, options_json,
                 answer_index, explanation, fingerprint, created_at)
            VALUES (#{programId}, #{userId}, #{form}, #{seq}, #{topic}, #{conceptId}, #{difficulty},
                    #{stem}, #{optionsJson}, #{answerIndex}, #{explanation}, #{fingerprint}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertItem(ItemRow row);

    /** 出题时读整卷（含答案，供判分用）。**只给服务层内部**，不进任何视图。 */
    @Select("""
            SELECT id, program_id, user_id, form, seq, topic, concept_id, difficulty, stem, options_json,
                   answer_index, explanation, fingerprint, created_at
            FROM camp_items
            WHERE program_id = #{programId} AND user_id = #{userId} AND form = #{form}
            ORDER BY seq
            """)
    List<ItemRow> listItems(@Param("programId") long programId, @Param("userId") long userId,
                            @Param("form") String form);

    /** 结果页用：题干 + 作答，不含答案（答案只用来算对错）。 */
    @Select("""
            SELECT i.seq, i.topic, i.difficulty, a.correct
            FROM camp_items i
            JOIN camp_attempts a ON a.item_id = i.id
            WHERE i.program_id = #{programId} AND i.user_id = #{userId} AND i.form = #{form}
            ORDER BY i.seq
            """)
    List<ScoredRow> listScored(@Param("programId") long programId, @Param("userId") long userId,
                               @Param("form") String form);

    @Delete("DELETE FROM camp_items WHERE program_id = #{programId} AND user_id = #{userId}")
    int deleteItems(@Param("programId") long programId, @Param("userId") long userId);

    // ── 作答 ────────────────────────────────────────────────────────

    @Insert("""
            INSERT INTO camp_attempts (program_id, user_id, item_id, choice, correct, created_at)
            VALUES (#{programId}, #{userId}, #{itemId}, #{choice}, #{correct}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAttempt(AttemptRow row);

    @Select("SELECT COUNT(*) FROM camp_attempts WHERE program_id = #{programId} AND user_id = #{userId}")
    int countAttempts(@Param("programId") long programId, @Param("userId") long userId);

    @Delete("DELETE FROM camp_attempts WHERE program_id = #{programId} AND user_id = #{userId}")
    int deleteAttempts(@Param("programId") long programId, @Param("userId") long userId);

    // ── 计划与任务 ──────────────────────────────────────────────────

    @Insert("""
            INSERT INTO camp_plans
                (program_id, user_id, days, budget_minutes, weak_json, unscheduled_json, created_at)
            VALUES (#{programId}, #{userId}, #{days}, #{budgetMinutes}, #{weakJson}, #{unscheduledJson},
                    #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPlan(PlanRow row);

    @Select("""
            SELECT id, program_id, user_id, days, budget_minutes, weak_json, unscheduled_json, created_at
            FROM camp_plans
            WHERE program_id = #{programId} AND user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
    PlanRow findLatestPlan(@Param("programId") long programId, @Param("userId") long userId);

    @Select("""
            SELECT id, program_id, user_id, days, budget_minutes, weak_json, unscheduled_json, created_at
            FROM camp_plans WHERE id = #{id} AND user_id = #{userId}
            """)
    PlanRow findPlan(@Param("id") long id, @Param("userId") long userId);

    @Insert("""
            INSERT INTO camp_tasks
                (plan_id, program_id, user_id, day_no, seq, kind, entry_id, concept_id, topic, title,
                 minutes, why, done_at, created_at)
            VALUES (#{planId}, #{programId}, #{userId}, #{dayNo}, #{seq}, #{kind}, #{entryId}, #{conceptId},
                    #{topic}, #{title}, #{minutes}, #{why}, NULL, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTask(TaskRow row);

    @Select("""
            SELECT id, plan_id, program_id, user_id, day_no, seq, kind, entry_id, concept_id, topic,
                   title, minutes, why, done_at, created_at
            FROM camp_tasks
            WHERE plan_id = #{planId} AND user_id = #{userId}
            ORDER BY day_no, seq
            """)
    List<TaskRow> listTasks(@Param("planId") long planId, @Param("userId") long userId);

    /**
     * 勾选/取消。**program_id 与 user_id 都在条件里**：少了任何一个，
     * 拿别人的 taskId 就能改别人的计划。
     */
    @Update("""
            UPDATE camp_tasks SET done_at = #{doneAt}
            WHERE id = #{taskId} AND program_id = #{programId} AND user_id = #{userId}
            """)
    int markTask(@Param("taskId") long taskId, @Param("programId") long programId,
                 @Param("userId") long userId, @Param("doneAt") Instant doneAt);

    @Delete("DELETE FROM camp_tasks WHERE program_id = #{programId} AND user_id = #{userId}")
    int deleteTasks(@Param("programId") long programId, @Param("userId") long userId);

    @Delete("DELETE FROM camp_plans WHERE program_id = #{programId} AND user_id = #{userId}")
    int deletePlans(@Param("programId") long programId, @Param("userId") long userId);

    /**
     * 逐（概念, 卷）的答对统计（WORK-029 掌握度联动）。
     *
     * <p>只取**已交卷**的：没交的卷子不算成绩。按 {@code concept_id} 判空过滤——
     * 模型没给出可用概念的题不参与图谱（REQ-033 的"没有概念映射的主题不进图谱"）。
     *
     * <p>一次查完所有卷子，在内存里取"每个概念最近那次"：卷数是个位数，
     * 用 SQL 窗口函数反而更难读。
     */
    @Select("""
            SELECT i.concept_id,
                   COALESCE(p.retest_submitted_at, p.diagnostic_submitted_at) AS submitted_at,
                   SUM(CASE WHEN a.correct = 1 THEN 1 ELSE 0 END) AS correct,
                   COUNT(*) AS total
            FROM camp_items i
            JOIN camp_attempts a ON a.item_id = i.id
            JOIN camp_programs p ON p.id = i.program_id
            WHERE i.user_id = #{userId} AND i.concept_id IS NOT NULL
            GROUP BY i.program_id, i.form, i.concept_id,
                     COALESCE(p.retest_submitted_at, p.diagnostic_submitted_at)
            """)
    List<ConceptTally> findConceptTallies(@Param("userId") long userId);

    /** 一个概念在一份卷子上的答对统计。 */
    record ConceptTally(long conceptId, Instant submittedAt, int correct, int total) {
    }

    // ── 行 ──────────────────────────────────────────────────────────

    class ProgramRow {
        private Long id;
        private Long userId;
        private Long resumeDocumentId;
        private String resumeTitle;
        private int days;
        private int itemCount;
        private int droppedCount;
        private Instant diagnosticSubmittedAt;
        private Instant retestSubmittedAt;
        private Long activePlanId;
        private Instant createdAt;
        private Instant updatedAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getResumeDocumentId() {
            return resumeDocumentId;
        }

        public void setResumeDocumentId(Long resumeDocumentId) {
            this.resumeDocumentId = resumeDocumentId;
        }

        public String getResumeTitle() {
            return resumeTitle;
        }

        public void setResumeTitle(String resumeTitle) {
            this.resumeTitle = resumeTitle;
        }

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        public int getDroppedCount() {
            return droppedCount;
        }

        public void setDroppedCount(int droppedCount) {
            this.droppedCount = droppedCount;
        }

        public Instant getDiagnosticSubmittedAt() {
            return diagnosticSubmittedAt;
        }

        public void setDiagnosticSubmittedAt(Instant diagnosticSubmittedAt) {
            this.diagnosticSubmittedAt = diagnosticSubmittedAt;
        }

        public Instant getRetestSubmittedAt() {
            return retestSubmittedAt;
        }

        public void setRetestSubmittedAt(Instant retestSubmittedAt) {
            this.retestSubmittedAt = retestSubmittedAt;
        }

        public Long getActivePlanId() {
            return activePlanId;
        }

        public void setActivePlanId(Long activePlanId) {
            this.activePlanId = activePlanId;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    class ItemRow {
        private Long id;
        private Long programId;
        private Long userId;
        private String form;
        private int seq;
        private String topic;
        private Long conceptId;
        private String difficulty;
        private String stem;
        private String optionsJson;
        private int answerIndex;
        private String explanation;
        private String fingerprint;
        private Instant createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProgramId() {
            return programId;
        }

        public void setProgramId(Long programId) {
            this.programId = programId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getForm() {
            return form;
        }

        public void setForm(String form) {
            this.form = form;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Long getConceptId() {
            return conceptId;
        }

        public void setConceptId(Long conceptId) {
            this.conceptId = conceptId;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public String getStem() {
            return stem;
        }

        public void setStem(String stem) {
            this.stem = stem;
        }

        public String getOptionsJson() {
            return optionsJson;
        }

        public void setOptionsJson(String optionsJson) {
            this.optionsJson = optionsJson;
        }

        public int getAnswerIndex() {
            return answerIndex;
        }

        public void setAnswerIndex(int answerIndex) {
            this.answerIndex = answerIndex;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

    /** 判分用的最小一行：只要"哪个主题、哪个难度、对没对"。 */
    record ScoredRow(int seq, String topic, String difficulty, boolean correct) {
    }

    class AttemptRow {
        private Long id;
        private Long programId;
        private Long userId;
        private Long itemId;
        private int choice;
        private boolean correct;
        private Instant createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProgramId() {
            return programId;
        }

        public void setProgramId(Long programId) {
            this.programId = programId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public int getChoice() {
            return choice;
        }

        public void setChoice(int choice) {
            this.choice = choice;
        }

        public boolean isCorrect() {
            return correct;
        }

        public void setCorrect(boolean correct) {
            this.correct = correct;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

    class PlanRow {
        private Long id;
        private Long programId;
        private Long userId;
        private int days;
        private int budgetMinutes;
        private String weakJson;
        private String unscheduledJson;
        private Instant createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProgramId() {
            return programId;
        }

        public void setProgramId(Long programId) {
            this.programId = programId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }

        public int getBudgetMinutes() {
            return budgetMinutes;
        }

        public void setBudgetMinutes(int budgetMinutes) {
            this.budgetMinutes = budgetMinutes;
        }

        public String getWeakJson() {
            return weakJson;
        }

        public void setWeakJson(String weakJson) {
            this.weakJson = weakJson;
        }

        public String getUnscheduledJson() {
            return unscheduledJson;
        }

        public void setUnscheduledJson(String unscheduledJson) {
            this.unscheduledJson = unscheduledJson;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

    class TaskRow {
        private Long id;
        private Long planId;
        private Long programId;
        private Long userId;
        private int dayNo;
        private int seq;
        private String kind;
        private Long entryId;
        private Long conceptId;
        private String topic;
        private String title;
        private int minutes;
        private String why;
        private Instant doneAt;
        private Instant createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getPlanId() {
            return planId;
        }

        public void setPlanId(Long planId) {
            this.planId = planId;
        }

        public Long getProgramId() {
            return programId;
        }

        public void setProgramId(Long programId) {
            this.programId = programId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public int getDayNo() {
            return dayNo;
        }

        public void setDayNo(int dayNo) {
            this.dayNo = dayNo;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public Long getEntryId() {
            return entryId;
        }

        public void setEntryId(Long entryId) {
            this.entryId = entryId;
        }

        public Long getConceptId() {
            return conceptId;
        }

        public void setConceptId(Long conceptId) {
            this.conceptId = conceptId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getMinutes() {
            return minutes;
        }

        public void setMinutes(int minutes) {
            this.minutes = minutes;
        }

        public String getWhy() {
            return why;
        }

        public void setWhy(String why) {
            this.why = why;
        }

        public Instant getDoneAt() {
            return doneAt;
        }

        public void setDoneAt(Instant doneAt) {
            this.doneAt = doneAt;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }
}
