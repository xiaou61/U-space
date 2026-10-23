package io.github.xiaou61.sandbox.internal.web;

import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.sandbox.internal.dataset.SqlDatasetMapper;
import io.github.xiaou61.sandbox.internal.dataset.SqlDatasetUploadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据集管理（WORK-035 T-09 / T-10）。
 *
 * <p>权限口径与沙箱一致：挂在 `/api/v1/sql-datasets/**`，只需"已登录"。归属一律取自令牌，
 * 请求体里没有 userId——**这是私人数据集不被越权的第一道**（第二道是 `SqlDatasets.visible`）。
 *
 * <p>管理端动作（下架、恢复）另外校验 ADMIN，见各自的注释。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/sql-datasets")
class SqlDatasetController {

    private final SqlDatasetUploadService uploads;
    private final SqlDatasetMapper mapper;

    SqlDatasetController(SqlDatasetUploadService uploads, SqlDatasetMapper mapper) {
        this.uploads = uploads;
        this.mapper = mapper;
            }

    /**
     * 上传一个私人数据集。
     *
     * <p>用 JSON 而不是 multipart：前端已经把文件读成文本了（CodeMirror 里也能直接写），
     * multipart 会多一层解析，而这个"文件"本来就是一串文本。
     */
    @PostMapping
    ApiResponse<CreatedView> upload(@Valid @RequestBody UploadRequest body) {
        long userId = currentUserId();
        String key = uploads.upload(userId, body.name(), body.sql());
        return ApiResponse.ok(new CreatedView(key));
    }

    /** 我的私人数据集（含未发布的草稿）。 */
    @GetMapping("/mine")
    ApiResponse<List<SqlDatasetMapper.Summary>> mine() {
        long userId = currentUserId();
        List<SqlDatasetMapper.Summary> rows = mapper.listVisible(userId).stream()
                .filter(row -> "private".equals(row.source()) && row.ownerUserId() != null
                        && row.ownerUserId() == userId)
                .toList();
        return ApiResponse.ok(rows);
    }

    /** 删除自己的私人数据集。 */
    @DeleteMapping("/{key}")
    ApiResponse<Void> delete(@PathVariable String key) {
        long userId = currentUserId();
        int removed = mapper.deleteOwned(key, userId);
        if (removed == 0) {
            // 不是自己的、或不存在——同一句文案，不给出可枚举的差异
            throw new BizException(ErrorCode.NOT_FOUND, "没有这个数据集");
        }
        return ApiResponse.ok(null);
    }

    /**
     * 发布为社区数据集（T-10）。
     *
     * <p>**先发布后审核**：发布立刻可见，出问题靠举报下架。这与 `note_shares` 的治理手段一致
     * （发布前人工审核会让上传变成瓶颈，而社区数据集的错误后果是"内容不当"，不是"系统损坏"）。
     */
    @PostMapping("/{key}/publish")
    ApiResponse<Void> publish(@PathVariable String key) {
        long userId = currentUserId();
        SqlDatasetMapper.Row row = mapper.findByKey(key)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "没有这个数据集"));
        if (row.ownerUserId() == null || row.ownerUserId() != userId) {
            throw new BizException(ErrorCode.NOT_FOUND, "没有这个数据集");
        }
        if (!"private".equals(row.source())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "只有私人数据集可以发布");
        }
        // 一条 UPDATE 同时改来源与状态：分两步会在中间留下"来源已是社区但状态还是 draft"的形态
        mapper.promoteToCommunity(key, java.time.Instant.now());
        return ApiResponse.ok(null);
    }

    /**
     * 举报一个社区数据集（T-10）。
     *
     * <p>任何人都能举报，包括作者自己（虽然没什么意义，但拒绝它只会多一个特例）。
     * 同一人对同一集重复举报只留一条。
     */
    @PostMapping("/{key}/report")
    ApiResponse<Void> report(@PathVariable String key, @Valid @RequestBody ReportRequest body) {
        long userId = currentUserId();
        SqlDatasetMapper.Row row = mapper.findByKey(key)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "没有这个数据集"));
        if (!"community".equals(row.source())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "只有社区数据集可以被举报");
        }
        mapper.upsertReport(key, userId, body.reason(), java.time.Instant.now());
        return ApiResponse.ok(null);
    }

    record CreatedView(String key) {
    }

    record UploadRequest(@NotBlank @Size(max = 100) String name,
                         @NotBlank @Size(max = 600_000) String sql) {
    }

    record ReportRequest(@NotBlank @Size(max = 500) String reason) {
    }

    /** 令牌主体 → 账号 id。与 SandboxController 同一口径（同一个 `CurrentUser` 工具）。 */
    private static long currentUserId() {
        try {
            return Long.parseLong(io.github.xiaou61.security.CurrentUser.subjectId());
        } catch (NumberFormatException cause) {
            throw new BizException(ErrorCode.UNAUTHENTICATED, "令牌主体不是有效的账号标识");
        }
    }
}
