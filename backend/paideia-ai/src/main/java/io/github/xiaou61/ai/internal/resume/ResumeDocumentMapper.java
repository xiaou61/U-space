package io.github.xiaou61.ai.internal.resume;

import java.time.Instant;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 主简历的读写（WORK-018 TASK-005）。
 *
 * <p><b>归属写在每一条 SQL 里</b>：凡是读用户数据的地方都带 `user_id`，漏写一处就等于把别人的
 * 简历读出去。这一条比"服务层校验一次"更可靠——服务层漏一次，SQL 层还能兜住。
 *
 * <p><b>保存用乐观锁</b>：`WHERE id = ? AND user_id = ? AND revision = ?`，命中 0 行就是
 * "别人改过了"。不用 `SELECT ... FOR UPDATE`：那会把并发编辑变成一个锁等待，而这里要的是
 * "告诉用户冲突了，让他选"。
 */
@Mapper
interface ResumeDocumentMapper {

    @Insert("""
            INSERT INTO resume_documents
                (user_id, title, is_default, revision, data_json, plain_text, created_at, updated_at)
            VALUES (#{userId}, #{title}, #{isDefault}, 1, #{dataJson}, #{plainText},
                    #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRow(ResumeDocumentRow row);

    @Select("""
            SELECT id, user_id, title, is_default, revision, data_json, plain_text, created_at, updated_at
            FROM resume_documents
            WHERE user_id = #{userId}
            ORDER BY is_default DESC, updated_at DESC, id DESC
            """)
    java.util.List<ResumeDocumentRow> listByUser(@Param("userId") long userId);

    @Select("""
            SELECT id, user_id, title, is_default, revision, data_json, plain_text, created_at, updated_at
            FROM resume_documents
            WHERE id = #{id} AND user_id = #{userId}
            """)
    ResumeDocumentRow findByIdAndUser(@Param("id") long id, @Param("userId") long userId);

    /**
     * 整份保存，带乐观锁。
     *
     * @return 命中行数；0 表示 revision 已被别人推进（或这份简历不属于他）
     */
    @Update("""
            UPDATE resume_documents
            SET title = #{title}, data_json = #{dataJson}, plain_text = #{plainText},
                revision = revision + 1, updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId} AND revision = #{revision}
            """)
    int updateWithRevision(ResumeDocumentRow row);

    /** 只改标题，不动 revision：改标题不是"改内容"，不该让并行的编辑器冲突。 */
    @Update("""
            UPDATE resume_documents
            SET title = #{title}, updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int rename(@Param("id") long id, @Param("userId") long userId,
               @Param("title") String title, @Param("updatedAt") Instant updatedAt);

    /** 设默认：先清同用户其他行，再把这一行置 1。两条语句都要在同一个事务里。 */
    @Update("UPDATE resume_documents SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1")
    int clearDefault(@Param("userId") long userId);

    @Update("""
            UPDATE resume_documents SET is_default = 1, updated_at = #{updatedAt}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int markDefault(@Param("id") long id, @Param("userId") long userId,
                    @Param("updatedAt") Instant updatedAt);

    @Delete("DELETE FROM resume_documents WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") long id, @Param("userId") long userId);

    /** 一行主简历（可变类 + `useGeneratedKeys`，与项目其它实体一致）。 */
    class ResumeDocumentRow {
        private Long id;
        private Long userId;
        private String title;
        private boolean isDefault;
        private int revision;
        private String dataJson;
        private String plainText;
        private Instant createdAt;
        private Instant updatedAt;

        Long getId() {
            return id;
        }

        void setId(Long id) {
            this.id = id;
        }

        Long getUserId() {
            return userId;
        }

        void setUserId(Long userId) {
            this.userId = userId;
        }

        String getTitle() {
            return title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        boolean isDefault() {
            return isDefault;
        }

        void setDefault(boolean aDefault) {
            this.isDefault = aDefault;
        }

        int getRevision() {
            return revision;
        }

        void setRevision(int revision) {
            this.revision = revision;
        }

        String getDataJson() {
            return dataJson;
        }

        void setDataJson(String dataJson) {
            this.dataJson = dataJson;
        }

        String getPlainText() {
            return plainText;
        }

        void setPlainText(String plainText) {
            this.plainText = plainText;
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
