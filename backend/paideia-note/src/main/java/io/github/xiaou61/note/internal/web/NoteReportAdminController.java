package io.github.xiaou61.note.internal.web;

import io.github.xiaou61.note.internal.share.ShareService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：举报列表与下架（用户裁决 11：举报与下架由管理员处理，不做自动判定）。
 *
 * <p>整个前缀由安全规则限定为 `hasRole("ADMIN")`，**而且那条规则必须写在更宽的规则之前**——
 * Spring Security 按声明顺序取第一个匹配的规则，顺序写反这条永不生效，且不会有任何报错，
 * 只会在某天表现为"任何已登录的学习者都能下架别人的内容"。
 *
 * <p>这里**不做自动判定**：下架是人做的决定，有举报列表让人判断就够了。
 */
@ApiAccess(ApiAccess.Access.ADMIN)
@RestController
@RequestMapping("/api/v1/notes/admin/reports")
class NoteReportAdminController {

    private final ShareService shares;

    NoteReportAdminController(ShareService shares) {
        this.shares = shares;
    }

    /** 列表。不带 `status` 看全部（待处理排在最前，同状态内早的在前）。 */
    @GetMapping
    ApiResponse<List<ShareService.ReportView>> list(@RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(shares.listReports(status));
    }

    /** 下架：把被举报的分享撤下来，并把这条举报标为已处理（同一个事务里）。 */
    @PostMapping("/{id}/takedown")
    ApiResponse<Boolean> takedown(@PathVariable("id") long reportId) {
        return ApiResponse.ok(shares.takedown(reportId));
    }
}
