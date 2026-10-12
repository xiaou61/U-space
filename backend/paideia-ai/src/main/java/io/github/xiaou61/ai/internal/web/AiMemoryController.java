package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.memory.MemoryStore;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 记忆的查看与逐条删除（用户裁决 13）。
 *
 * <p>两个动作都是**本人范围**：没有"看别人的记忆"的端点，也没有分享开关——记忆严格私有
 * （用户裁决 3）。这条不是靠调用方自觉，而是这个控制器上就没有那样的路径。
 *
 * <p>删除路径里带 `source`：两路的 id 各自独立，只说 id 会删错表。
 */
@RestController
@RequestMapping("/api/v1/ai/memories")
class AiMemoryController {

    /** 界面形状。`source` 要回给前端，否则删的时候没法说明删的是哪一路。 */
    record MemoryView(String source, long id, String text, Instant createdAt) {
    }

    private final MemoryStore memories;

    AiMemoryController(MemoryStore memories) {
        this.memories = memories;
    }

    @GetMapping
    ApiResponse<List<MemoryView>> list() {
        return ApiResponse.ok(memories.list(AiRequester.require()).stream()
                .map(item -> new MemoryView(item.source().name().toLowerCase(java.util.Locale.ROOT),
                        item.id(), item.text(), item.createdAt()))
                .toList());
    }

    @DeleteMapping("/{source}/{id}")
    ApiResponse<Boolean> delete(@PathVariable("source") String source, @PathVariable("id") long id) {
        long userId = AiRequester.require();
        MemoryStore.MemoryItem.Source parsed = parseSource(source);
        // 删不到（不是本人的、或本来就不存在）都返回 false，**不区分**——区分开就等于
        // 确认"某个 id 存在"。前端据此刷新列表即可。
        return ApiResponse.ok(memories.delete(userId, parsed, id));
    }

    private static MemoryStore.MemoryItem.Source parseSource(String raw) {
        return switch (raw == null ? "" : raw.toLowerCase(java.util.Locale.ROOT)) {
            case "structured" -> MemoryStore.MemoryItem.Source.STRUCTURED;
            case "text" -> MemoryStore.MemoryItem.Source.TEXT;
            default -> throw new BizException(ErrorCode.INVALID_ARGUMENT, "来源只能是 structured 或 text");
        };
    }
}
