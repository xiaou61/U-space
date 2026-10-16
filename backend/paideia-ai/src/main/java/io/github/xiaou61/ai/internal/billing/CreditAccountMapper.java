package io.github.xiaou61.ai.internal.billing;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 积分余额的读写。
 *
 * <p><b>扣减是一条带条件的 UPDATE</b>（{@code balance >= amount}），不是"先查后写"：
 * 并发下"查到的余额"必然过期，只有这条 SQL 的影响行数能当判据。这也是 AC-004（并发不超扣）
 * 唯一的依据。
 *
 * <p>没有账户行 = 余额 0（新用户不必先建行）：所以 {@link #deductIfEnough} 在无行时返回 0，
 * 语义与"余额不足"一致，不需要额外分支。
 */
@Mapper
interface CreditAccountMapper {

    @Select("SELECT balance FROM ai_credit_accounts WHERE user_id = #{userId}")
    Long findBalance(@Param("userId") long userId);

    /**
     * 原子扣减。
     *
     * @return 影响行数；0 表示余额不足（或还没有账户行）
     */
    @Update("""
            UPDATE ai_credit_accounts
            SET balance = balance - #{amount}
            WHERE user_id = #{userId} AND balance >= #{amount}
            """)
    int deductIfEnough(@Param("userId") long userId, @Param("amount") long amount);

    /** 入账（加余额）。账户行不存在时**顺手建行**——管理员给新用户发积分不该先要求那行存在。 */
    @Insert("""
            INSERT INTO ai_credit_accounts (user_id, balance)
            VALUES (#{userId}, #{amount})
            ON DUPLICATE KEY UPDATE balance = balance + VALUES(balance)
            """)
    int add(@Param("userId") long userId, @Param("amount") long amount);
}
