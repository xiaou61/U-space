package io.github.xiaou61.ai.internal.billing;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分流水的读写。
 *
 * <p>**只 insert 与 select，没有 update / delete**（注销时的整行删除由 {@code AiUserDeletionListener}
 * 用 JDBC 做，不走这里）。审计要的是"发生过什么"，一条会被改写的流水等于没有流水。
 */
@Mapper
interface CreditLedgerMapper {

    @Insert("""
            INSERT INTO ai_credit_ledger
              (user_id, delta, kind, model, input_tokens, output_tokens, ref_message_id, operator_user_id, note)
            VALUES
              (#{userId}, #{delta}, #{kind}, #{model}, #{inputTokens}, #{outputTokens},
               #{refMessageId}, #{operatorUserId}, #{note})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditLedger row);

    @Select("""
            SELECT id, user_id, delta, kind, model, input_tokens, output_tokens,
                   ref_message_id, operator_user_id, note, created_at
            FROM ai_credit_ledger
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<CreditLedger> listByUser(@Param("userId") long userId, @Param("limit") int limit);

    /** 管理端审计页：全平台最近的流水。 */
    @Select("""
            SELECT id, user_id, delta, kind, model, input_tokens, output_tokens,
                   ref_message_id, operator_user_id, note, created_at
            FROM ai_credit_ledger
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<CreditLedger> listRecent(@Param("limit") int limit);
}
