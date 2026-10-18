package io.github.xiaou61.account.internal.profile;

import io.github.xiaou61.account.LearnerProfile;
import java.time.Instant;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学习者画像的读写（WORK-010 T-01/T-02）。
 *
 * <p>**没有"读别人的画像"这条路径**：主键就是 `user_id`，查询与写入都只能针对本人。
 *
 * <p>写入用 upsert 而不是"先查再插或改"：两步之间会有并发窗口，而画像是**整行覆盖**的语义，
 * 一条语句既原子又不需要事务包装。
 *
 * <p>`updated_at` 由调用方传入（按 UTC），不用库的 `ON UPDATE CURRENT_TIMESTAMP`——库会话时区是
 * +08:00，默认值会让同一列出现两种时区（2026-10-10 踩过）。
 */
@Mapper
public interface LearnerProfileMapper {

    @Select("""
            SELECT identity, target_role, target_city, timeline, stage, updated_at
            FROM learner_profiles
            WHERE user_id = #{userId}
            """)
    Optional<LearnerProfileRow> find(@Param("userId") long userId);

    @Insert("""
            INSERT INTO learner_profiles
                (user_id, identity, target_role, target_city, timeline, stage, updated_at)
            VALUES
                (#{userId}, #{identity}, #{targetRole}, #{targetCity}, #{timeline}, #{stage}, #{updatedAt})
            ON DUPLICATE KEY UPDATE
                identity = VALUES(identity),
                target_role = VALUES(target_role),
                target_city = VALUES(target_city),
                timeline = VALUES(timeline),
                stage = VALUES(stage),
                updated_at = VALUES(updated_at)
            """)
    int upsert(@Param("userId") long userId,
               @Param("identity") String identity,
               @Param("targetRole") String targetRole,
               @Param("targetCity") String targetCity,
               @Param("timeline") String timeline,
               @Param("stage") String stage,
               @Param("updatedAt") Instant updatedAt);

    /** 注销级联用（其它表也一样：清单在 {@code AccountDeletionCascadeIntegrationTest} 里被逐表断言）。 */
    @org.apache.ibatis.annotations.Delete("DELETE FROM learner_profiles WHERE user_id = #{userId}")
    int deleteByUser(@Param("userId") long userId);

    /** 库里的原始行。`identity` 是字符串，由服务层规范成枚举（脏值退回默认，不抛错）。 */
    record LearnerProfileRow(String identity, String targetRole, String targetCity,
                             String timeline, String stage, Instant updatedAt) {

        LearnerProfile toProfile(io.github.xiaou61.account.Identity normalized) {
            return new LearnerProfile(normalized, targetRole, targetCity, timeline, stage, updatedAt);
        }
    }
}
