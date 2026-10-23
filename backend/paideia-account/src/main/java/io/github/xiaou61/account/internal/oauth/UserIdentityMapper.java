package io.github.xiaou61.account.internal.oauth;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 第三方登录绑定表（WORK-037）。
 *
 * <p>唯一性由数据库的唯一索引 {@code (provider, provider_user_id)} 兜底，这里的查询只用于
 * 给出友好提示与做前置判断；并发插入靠捕获冲突，而不是"先查再插"。
 */
@Mapper
public interface UserIdentityMapper {

    @Insert("""
            INSERT INTO user_identities (user_id, provider, provider_user_id, display_name)
            VALUES (#{userId}, #{provider}, #{providerUserId}, #{displayName})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserIdentity identity);

    /** 登录时用：这个平台账号绑在谁身上。 */
    @Select("""
            SELECT id, user_id, provider, provider_user_id, display_name, created_at
            FROM user_identities WHERE provider = #{provider} AND provider_user_id = #{providerUserId}
            """)
    Optional<UserIdentity> findByProviderAndSubject(@Param("provider") String provider,
                                                    @Param("providerUserId") String providerUserId);

    /** 账号设置页用：这个人绑了哪些。 */
    @Select("""
            SELECT id, user_id, provider, provider_user_id, display_name, created_at
            FROM user_identities WHERE user_id = #{userId} ORDER BY created_at
            """)
    List<UserIdentity> findByUserId(@Param("userId") long userId);

    /** 解绑。返回 0 表示本来就没绑，调用方按"无变化"处理。 */
    @Delete("""
            DELETE FROM user_identities WHERE user_id = #{userId} AND provider = #{provider}
            """)
    int deleteByUserIdAndProvider(@Param("userId") long userId, @Param("provider") String provider);

    /**
     * 这个账号还有几种第三方登录方式。
     *
     * <p>解绑前要算"解完还剩没剩"，所以数的是**别人的**数量：调用方加上"有没有密码"
     * 才是完整的可用方式数。
     */
    @Select("SELECT COUNT(*) FROM user_identities WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") long userId);
}
