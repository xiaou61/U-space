package io.github.xiaou61;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 种子数据的 id **现查**，不在测试里写死。
 *
 * <p><b>为什么</b>：写死 id 的断言会被"同一个库上的其他写入"连累。2026-10-18 全量跑时出现过
 * `KnowledgeRead` / `KnowledgeSelfAssessment` 报"读不到种子条目 id=1"——那条被同机另一个会话的
 * 测试真删掉了，而每个类单独跑都是绿的。改成按标题/slug 查，删掉别的行不会让断言指向空。
 *
 * <p>一并说明：查不到会直接抛 {@code IncorrectResultSizeDataAccessException}（"Expected 1 but found 0"）
 * ——那正是想要的失败方式，比 NPE 或"断言了一个不存在的 id 而看起来通过"清楚得多。
 *
 * <p>种子本身见 {@code db/devdata/V951}、{@code V952}。
 */
final class SeedIds {

    private final JdbcTemplate jdbc;

    SeedIds(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    long entry(String title) {
        return jdbc.queryForObject("SELECT id FROM knowledge_entries WHERE title = ?", Long.class, title);
    }

    long category(String slug) {
        return jdbc.queryForObject("SELECT id FROM knowledge_categories WHERE slug = ?", Long.class, slug);
    }
}
