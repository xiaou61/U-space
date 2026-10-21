package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** RSS/Atom 解析与 XXE 防护（WORK-027 TASK-003）。 */
class FeedXmlTest {

    private final FeedXml feedXml = new FeedXml();

    private static final long FALLBACK = 1_800_000_000_000L;

    @Test
    @DisplayName("正常 RSS：标题、链接、时间、摘要都对")
    void parsesNormalRss() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0"><channel>
                  <title>量子位</title>
                  <item>
                    <title>Claude 新模型曝光</title>
                    <link>https://www.qbitai.com/2026/10/claude.html</link>
                    <pubDate>Tue, 20 Oct 2026 10:00:00 +0800</pubDate>
                    <description>&lt;p&gt;这是一段&lt;/p&gt;&lt;p&gt;描述&lt;/p&gt;</description>
                  </item>
                  <item>
                    <title>第二条</title>
                    <link>https://www.qbitai.com/2026/10/second.html</link>
                    <pubDate>Wed, 21 Oct 2026 09:00:00 +0800</pubDate>
                    <description>简短描述</description>
                  </item>
                </channel></rss>
                """;

        List<FeedXml.Entry> entries = feedXml.parse(xml, FALLBACK);

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).title()).isEqualTo("Claude 新模型曝光");
        assertThat(entries.get(0).link()).isEqualTo("https://www.qbitai.com/2026/10/claude.html");
        assertThat(entries.get(0).summaryHint()).isEqualTo("这是一段 描述");
        assertThat(entries.get(0).publishedAt()).isLessThan(entries.get(1).publishedAt());
    }

    @Test
    @DisplayName("一条 pubDate 坏掉只跳过该条的时间，好条目仍在")
    void brokenPubDateDoesNotKillTheBatch() {
        String xml = """
                <?xml version="1.0"?>
                <rss version="2.0"><channel>
                  <item>
                    <title>时间坏掉</title>
                    <link>https://example.com/broken</link>
                    <pubDate>这不是一个时间</pubDate>
                  </item>
                  <item>
                    <title>正常条目</title>
                    <link>https://example.com/good</link>
                    <pubDate>Wed, 21 Oct 2026 09:00:00 +0800</pubDate>
                  </item>
                </channel></rss>
                """;

        List<FeedXml.Entry> entries = feedXml.parse(xml, FALLBACK);

        // 两条都保留：坏时间的退回 fallback，不整批失败
        assertThat(entries).hasSize(2);
        assertThat(entries).extracting(FeedXml.Entry::title)
                .containsExactly("时间坏掉", "正常条目");
        assertThat(entries.get(0).publishedAt()).isEqualTo(FALLBACK);
        assertThat(entries.get(1).publishedAt()).isNotEqualTo(FALLBACK);
    }

    @Test
    @DisplayName("Atom：取 alternate 链接与 published 时间")
    void parsesAtom() {
        String xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <title>InfoQ</title>
                  <entry>
                    <title>某篇技术文章</title>
                    <link rel="alternate" href="https://www.infoq.cn/article/abc"/>
                    <link rel="self" href="https://www.infoq.cn/feed/self"/>
                    <published>2026-10-21T01:00:00Z</published>
                    <summary>文章摘要内容</summary>
                  </entry>
                </feed>
                """;

        List<FeedXml.Entry> entries = feedXml.parse(xml, FALLBACK);

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).title()).isEqualTo("某篇技术文章");
        assertThat(entries.get(0).link()).isEqualTo("https://www.infoq.cn/article/abc");
        assertThat(entries.get(0).summaryHint()).isEqualTo("文章摘要内容");
    }

    @Test
    @DisplayName("**XXE：带 DOCTYPE 实体的 RSS 必须被拒绝，且读不到任何文件内容**")
    void rejectsXxePayload() {
        String payload = """
                <?xml version="1.0"?>
                <!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <rss version="2.0"><channel>
                  <item>
                    <title>&xxe;</title>
                    <link>https://example.com/evil</link>
                  </item>
                </channel></rss>
                """;

        assertThatThrownBy(() -> feedXml.parse(payload, FALLBACK))
                .isInstanceOf(DigestSource.DigestSourceException.class)
                .hasMessageContaining("XML 解析被拒绝");
    }

    @Test
    @DisplayName("XXE：外网实体同样被拒（不发起请求）")
    void rejectsExternalEntity() {
        String payload = """
                <?xml version="1.0"?>
                <!DOCTYPE rss [<!ENTITY xxe SYSTEM "http://127.0.0.1:9999/secret">]>
                <rss version="2.0"><channel><item>
                  <title>&xxe;</title><link>https://example.com/x</link>
                </item></channel></rss>
                """;

        assertThatThrownBy(() -> feedXml.parse(payload, FALLBACK))
                .isInstanceOf(DigestSource.DigestSourceException.class);
    }

    @Test
    @DisplayName("星期几写错也能解析出时间（源常犯，不能因此丢条目）")
    void wrongWeekdayStillParses() {
        // 2026-10-20 实际是周二；这条写成周一，严格模式会因 "DayOfWeek differs" 失败
        String xml = """
                <?xml version="1.0"?>
                <rss version="2.0"><channel><item>
                  <title>星期几写错</title>
                  <link>https://example.com/wrong-weekday</link>
                  <pubDate>Mon, 20 Oct 2026 10:00:00 +0800</pubDate>
                </item></channel></rss>
                """;

        List<FeedXml.Entry> entries = feedXml.parse(xml, FALLBACK);

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).publishedAt())
                .as("时间应被解析出来而不是退回 fallback")
                .isNotEqualTo(FALLBACK);
    }

    @Test
    @DisplayName("没有链接的条目被丢弃（无法去重也无法点开）")
    void dropsEntriesWithoutLink() {
        String xml = """
                <?xml version="1.0"?>
                <rss version="2.0"><channel>
                  <item><title>没有链接</title></item>
                  <item><title>有链接</title><link>https://example.com/ok</link></item>
                </channel></rss>
                """;

        assertThat(feedXml.parse(xml, FALLBACK))
                .extracting(FeedXml.Entry::title).containsExactly("有链接");
    }

    @Test
    @DisplayName("不认识的根元素返回空列表，不抛异常")
    void unknownRootIsEmpty() {
        assertThat(feedXml.parse("<html><body/></html>", FALLBACK)).isEmpty();
    }

    @Test
    @DisplayName("摘要线索截断到 200 字")
    void truncatesHint() {
        String longDescription = "字".repeat(500);
        String xml = """
                <?xml version="1.0"?>
                <rss version="2.0"><channel><item>
                  <title>t</title><link>https://example.com/long</link>
                  <description>%s</description>
                </item></channel></rss>
                """.formatted(longDescription);

        List<FeedXml.Entry> entries = feedXml.parse(xml, FALLBACK);
        assertThat(entries.get(0).summaryHint()).hasSize(200);
    }
}
