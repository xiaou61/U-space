package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.settings.AiSettingsStore;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 设置的读写（WORK-013）。
 *
 * <p>三个开关各自在别处生效，这里只管它们存在与怎么改：
 * <ul>
 *   <li>{@code memoryEnabled} → 召回短路、自动整理的闸、手动写入的拒绝（{@code AiMemoryController}）</li>
 *   <li>{@code referenceSavedMemories} → 只挡召回（不管写入：整理是"写"不是"用"）</li>
 *   <li>{@code referenceChatHistory} → 只挡"把历史消息拼进提示词"</li>
 * </ul>
 *
 * <p><b>请求体用包装类型 {@link UpdateRequest}</b>：三个布尔若是基本类型，客户端漏传一个字段时
 * Jackson 会给 `false`——那等于"用户没改这个开关"被当成"用户关掉了它"。宁可 400。
 *
 * <p>归属取调用者：请求体里没有 userId，传不了别人的设置（与记忆那套一致）。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/settings")
class AiSettingsController {

    private final AiSettingsStore settings;

    AiSettingsController(AiSettingsStore settings) {
        this.settings = settings;
    }

    /** `PUT` 的请求体。三个都要给（整行覆盖），缺一个就拒绝。 */
    record UpdateRequest(Boolean memoryEnabled, Boolean referenceSavedMemories, Boolean referenceChatHistory) {
    }

    @GetMapping
    ApiResponse<AiSettingsStore.Settings> get() {
        return ApiResponse.ok(settings.get(AiRequester.require()));
    }

    @PutMapping
    ApiResponse<AiSettingsStore.Settings> update(@RequestBody(required = false) UpdateRequest request) {
        long userId = AiRequester.require();
        if (request == null || request.memoryEnabled() == null
                || request.referenceSavedMemories() == null || request.referenceChatHistory() == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "三个开关都要给（整行覆盖）");
        }
        return ApiResponse.ok(settings.update(userId, new AiSettingsStore.Settings(
                request.memoryEnabled(), request.referenceSavedMemories(), request.referenceChatHistory())));
    }
}
