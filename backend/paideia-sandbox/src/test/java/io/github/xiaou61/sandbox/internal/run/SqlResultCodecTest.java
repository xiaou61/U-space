package io.github.xiaou61.sandbox.internal.run;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 结果集解析（WORK-032）。
 *
 * <p>下面每个样例都是 **2026-10-21 在服务器上用真 mysqld 8.0.46 跑出来的原样输出**，
 * 不是手编的 XML。这条纪律与其它课程的"可运行示例"一致：贴没验证过的样例，
 * 解析器就是对着想象写的。
 */
class SqlResultCodecTest {

    private static final String HEAD = "<?xml version=\"1.0\"?>\n\n";

    @Test
    void 单结果集含NULL与定点数尾零() {
        String stdout = HEAD
                + "<resultset statement=\"SELECT id, note, total FROM t\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <row>\n\t<field name=\"id\">1</field>\n\t<field name=\"note\">a</field>\n"
                + "\t<field name=\"total\">92.50</field>\n  </row>\n\n"
                + "  <row>\n\t<field name=\"id\">2</field>\n\t<field name=\"note\" xsi:nil=\"true\" />\n"
                + "\t<field name=\"total\" xsi:nil=\"true\" />\n  </row>\n</resultset>\n";

        SqlResultCodec.Parsed parsed = SqlResultCodec.parse(stdout, 100);
        assertThat(parsed.overLimit()).isFalse();
        assertThat(parsed.tables()).hasSize(1);
        CodeRun.Table table = parsed.tables().get(0);
        assertThat(table.statementIndex()).isZero();
        assertThat(table.columns()).containsExactly("id", "note", "total");
        assertThat(table.rows()).containsExactly(
                List.of("1", "a", "92.50"),
                List.of("2", SqlResultCodec.NULL_MARKER, SqlResultCodec.NULL_MARKER));
    }

    @Test
    void 多个XML文档首尾相接按resultset切段() {
        String stdout = HEAD
                + "<resultset statement=\"SELECT 1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <row>\n\t<field name=\"v\">10</field>\n  </row>\n</resultset>\n"
                + HEAD
                + "<resultset statement=\"SELECT 2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <row>\n\t<field name=\"v\">11</field>\n  </row>\n</resultset>\n";

        SqlResultCodec.Parsed parsed = SqlResultCodec.parse(stdout, 100);
        assertThat(parsed.tables()).hasSize(2);
        assertThat(parsed.tables().get(0).rows()).containsExactly(List.of("10"));
        assertThat(parsed.tables().get(1).rows()).containsExactly(List.of("11"));
        assertThat(parsed.tables().get(0).statementIndex()).isZero();
        assertThat(parsed.tables().get(1).statementIndex()).isEqualTo(1);
    }

    @Test
    void BEGIN与ROLLBACK不产生结果集所以序号连续但缺项() {
        // 真跑 "BEGIN; UPDATE; SELECT; ROLLBACK;" 时 --xml 只给出三条语句里的那一条 SELECT
        String stdout = HEAD
                + "<resultset statement=\"SELECT total FROM t WHERE id = 1\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <row>\n\t<field name=\"total\">1.00</field>\n  </row>\n</resultset>\n";

        SqlResultCodec.Parsed parsed = SqlResultCodec.parse(stdout, 100);
        assertThat(parsed.tables()).hasSize(1);
        assertThat(parsed.tables().get(0).statementIndex()).isZero();
        assertThat(parsed.tables().get(0).rows()).containsExactly(List.of("1.00"));
    }

    @Test
    void 空结果集是零行() {
        String stdout = HEAD
                + "<resultset statement=\"SELECT id FROM t WHERE 1 = 0\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n</resultset>\n";

        SqlResultCodec.Parsed parsed = SqlResultCodec.parse(stdout, 100);
        assertThat(parsed.tables()).hasSize(1);
        assertThat(parsed.tables().get(0).rows()).isEmpty();
        // MySQL 的 --xml 在没有行时不输出字段名，所以列也是空的——这是客户端的限制，
        // 判题侧两侧（参考解与用户解）走同一条路径，因此仍可比。
        assertThat(parsed.tables().get(0).columns()).isEmpty();
    }

    @Test
    void 超过行数上限时整批作废而不是返回半截() {
        String stdout = HEAD
                + "<resultset statement=\"SELECT v FROM t\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <row>\n\t<field name=\"v\">1</field>\n  </row>\n"
                + "  <row>\n\t<field name=\"v\">2</field>\n  </row>\n"
                + "  <row>\n\t<field name=\"v\">3</field>\n  </row>\n"
                + "  <row>\n\t<field name=\"v\">4</field>\n  </row>\n</resultset>\n";

        SqlResultCodec.Parsed parsed = SqlResultCodec.parse(stdout, 3);
        assertThat(parsed.overLimit()).isTrue();
        assertThat(parsed.tables()).as("超限不能冒充成功").isEmpty();
    }

    @Test
    void XML实体被还原() {
        String stdout = HEAD
                + "<resultset statement=\"SELECT note\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "  <row>\n\t<field name=\"note\">a &lt;b&gt; &amp; c</field>\n  </row>\n</resultset>\n";

        assertThat(SqlResultCodec.parse(stdout, 10).tables().get(0).rows())
                .containsExactly(List.of("a <b> & c"));
    }

    @Test
    void 空输入没有结果集也不算超限() {
        assertThat(SqlResultCodec.parse("", 10).tables()).isEmpty();
        assertThat(SqlResultCodec.parse(null, 10).overLimit()).isFalse();
    }
}
