package io.github.xiaou61.note.internal.note;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 笔记的读写。
 *
 * <p><b>归属写在 SQL 里</b>，与项目其它模块同一纪律：每一条语句都带 {@code user_id}。
 * 笔记是最私有的那种数据（用户自己写的），漏一处就等于把别人的笔记读出去——
 * 写在 WHERE 里就不存在"忘了判断"这条路径。
 *
 * <p>读取用 {@code Optional}：调用方拿不到就当"不存在"，**不区分**"没有这条"与"不是你的"。
 * 区分开就等于确认"某个 id 存在"。
 */
@Mapper
interface NoteMapper {

    @Insert("""
            INSERT INTO notes (user_id, entry_id, title, body)
            VALUES (#{userId}, #{entryId}, #{title}, #{body})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Note note);

    @Select("""
            SELECT id, user_id, entry_id, title, body, created_at, updated_at
            FROM notes
            WHERE id = #{id} AND user_id = #{userId}
            """)
    Optional<Note> findByIdAndUser(@Param("id") long id, @Param("userId") long userId);

    /** 列表不带正文：列表页只要标题与时间，把正文一起取回来会让一个几百条的列表变得很重。 */
    @Select("""
            SELECT id, user_id, entry_id, title, created_at, updated_at
            FROM notes
            WHERE user_id = #{userId}
            ORDER BY updated_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<Note> listByUser(@Param("userId") long userId, @Param("limit") int limit);

    @Update("""
            UPDATE notes
            SET title = #{title}, body = #{body}, entry_id = #{entryId}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int update(Note note);

    @Delete("DELETE FROM notes WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") long id, @Param("userId") long userId);
}
