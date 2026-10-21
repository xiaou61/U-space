package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Hacker News 源（WORK-027 TASK-004）。 */
class HackerNewsSourceTest {

    private final HackerNewsSource source = new HackerNewsSource();

    private static DigestHttp httpReturning(String body) {
        return new DigestHttp(5) {
            @Override
            String getTextWithHeaders(String url, String userAgent, Map<String, String> headers) {
                return body;
            }
        };
    }

    @Test
    @DisplayName("解析 hits：标题、链接、points 作为 heat、评论数进 meta")
    void parsesHits() {
        String json = """
                {"hits":[
                  {"title":"Show HN: something","url":"https://example.com/x",
                   "points":321,"num_comments":45,"objectID":"1","created_at_i":1790000000},
                  {"title":"Ask HN: question","url":null,
                   "points":10,"num_comments":3,"objectID":"42","created_at_i":1790000100}
                ]}
                """;

        List<FetchedItem> items = source.fetch(httpReturning(json), 1_800_000_000_000L);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).heat()).isEqualTo(321);
        assertThat(items.get(0).meta()).isEqualTo("45 条评论");
        assertThat(items.get(0).groupKey()).isEqualTo("hackernews");
        assertThat(items.get(0).publishedAt()).isEqualTo(1_790_000_000_000L);
    }

    @Test
    @DisplayName("没有外链的讨论帖用 HN 讨论页兜住")
    void discussionWithoutUrlUsesHnPage() {
        String json = """
                {"hits":[{"title":"Ask HN: no link","url":null,"points":5,
                          "num_comments":0,"objectID":"777","created_at_i":1790000000}]}
                """;

        List<FetchedItem> items = source.fetch(httpReturning(json), 1_800_000_000_000L);

        assertThat(items.get(0).url())
                .isEqualTo("https://news.ycombinator.com/item?id=777");
        assertThat(items.get(0).meta()).as("没有评论时不写 meta").isNull();
    }

    @Test
    @DisplayName("JSON 坏掉抛 DigestSourceException，由编排层单源接住")
    void malformedJsonThrows() {
        assertThatThrownBy(() -> source.fetch(httpReturning("不是 JSON"), 1_800_000_000_000L))
                .isInstanceOf(DigestSource.DigestSourceException.class);
    }

    @Test
    @DisplayName("网络失败同样抛出，不吞成一个空列表（日志要点名是哪个源）")
    void networkFailurePropagates() {
        DigestHttp failing = new DigestHttp(5) {
            @Override
            String getTextWithHeaders(String url, String userAgent, Map<String, String> headers) {
                throw new DigestSource.DigestSourceException("HTTP 503");
            }
        };

        assertThatThrownBy(() -> source.fetch(failing, 1_800_000_000_000L))
                .isInstanceOf(DigestSource.DigestSourceException.class)
                .hasMessageContaining("503");
    }
}
