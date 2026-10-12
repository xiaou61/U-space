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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 写入请求。
     *
     * <p>`type` 必给：两路存在不同的表里，只给一段文本无法判断该进哪一路。
     * `kind`／`refId` 只有结构化那路用得上。
     */
    record WriteRequest(String type, String text, String kind, Long refId) {
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

    /**
     * 写入一条记忆。**这是记忆唯一的写入口**。
     *
     * <p>为什么单独成一个端点而不是让模型直接写：用户裁决 7 是"自主只建议不动作"，
     * 所以模型产出的只能是**草案**，由用户点击后调到这里来。模型那条路径不存在——
     * 这是"写操作必须用户确认"在接口层的落点，不是靠前端自觉。
     *
     * <p>归属取调用者：请求体里**没有** userId 字段，传不了别人的。
     */
    @PostMapping
    ApiResponse<Long> create(@RequestBody WriteRequest request) {
        long userId = AiRequester.require();
        if (request == null || request.type() == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "类型只能是 structured 或 text");
        }
        return switch (request.type().toLowerCase(java.util.Locale.ROOT)) {
            case "structured" -> ApiResponse.ok(
                    memories.rememberStructured(userId, request.kind(), request.refId(), request.text()));
            case "text" -> ApiResponse.ok(memories.rememberText(userId, request.text()));
            default -> throw new BizException(ErrorCode.INVALID_ARGUMENT, "类型只能是 structured 或 text");
        };
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
