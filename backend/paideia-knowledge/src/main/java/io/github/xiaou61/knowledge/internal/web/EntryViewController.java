package io.github.xiaou61.knowledge.internal.web;

import io.github.xiaou61.knowledge.internal.entry.EntryViewService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 浏览上报（WORK-021）。
 *
 * <p>与自评同一条纪律：**路径与请求体里没有用户标识**，归属取自令牌。
 *
 * <p>用 POST 而不是 GET：它写数据；浏览器预取与缓存都不该产生计数。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/knowledge")
class EntryViewController {

    private final EntryViewService views;

    EntryViewController(EntryViewService views) {
        this.views = views;
    }

    /** 记一次浏览。未发布条目 404，且不写行。 */
    @PostMapping("/entries/{id}/view")
    ApiResponse<Void> record(@PathVariable("id") long id) {
        views.record(CurrentUserId.require(), id);
        return ApiResponse.ok(null);
    }
}
