package io.github.xiaou61.ai.internal.credential;

import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户模型凭据的读写。SQL 只写在这里（沿用本项目 {@code @Mapper} + 注解的写法，不用 XML）。
 *
 * <p>upsert 用 MySQL 8.0.19+ 的别名写法（{@code AS incoming}）而不是已废弃的 {@code VALUES()}：
 * 后者在 8.0.20 起被标记废弃，而本项目用的正是 8.0.46。
 */
@Mapper
interface ModelCredentialMapper {

    @Select("""
            SELECT id, user_id, base_url, model, api_key_cipher, api_key_last4, created_at, updated_at
            FROM ai_model_credentials WHERE user_id = #{userId}
            """)
    Optional<ModelCredential> findByUser(@Param("userId") long userId);

    @Insert("""
            INSERT INTO ai_model_credentials (user_id, base_url, model, api_key_cipher, api_key_last4)
            VALUES (#{userId}, #{baseUrl}, #{model}, #{apiKeyCipher}, #{apiKeyLast4}) AS incoming
            ON DUPLICATE KEY UPDATE
                base_url = incoming.base_url,
                model = incoming.model,
                api_key_cipher = incoming.api_key_cipher,
                api_key_last4 = incoming.api_key_last4
            """)
    int upsert(ModelCredential credential);

    @Delete("DELETE FROM ai_model_credentials WHERE user_id = #{userId}")
    int deleteByUser(@Param("userId") long userId);
}
