package io.github.xiaou61.ai.internal.digest.web;

import io.github.xiaou61.ai.internal.digest.DigestMapper;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端的速递条目管理（WORK-027）。
 *
 * <p><b>路径是 {@code /api/v1/digest/admin/**}，不是 {@code /api/v1/admin/digest}。</b>
 * 项目现有四条管理端规则全是「模块/admin」；写成后者会没有任何规则匹配，
 * 直接落到 {@code anyRequest().authenticated()}——**任何登录用户都能删条目**，而且不报错。
 * 这条规则必须排在更宽的 {@code /api/v1/digest/**} 之前（见 `SecurityConfiguration`）。
 *
 * <p><b>删除是硬删</b>：抓来的公开新闻没有审计价值。删掉之后同一天重跑**会**再抓回来
 * （指纹已不在库里），这是已知行为——要做永久屏蔽得另做黑名单，一期不做。
 */
@ApiAccess(ApiAccess.Access.ADMIN)
@RestController
@RequestMapping("/api/v1/digest/admin")
class DigestAdminController {

    private final DigestMapper mapper;

    DigestAdminController(DigestMapper mapper) {
        this.mapper = mapper;
    }

    /** 当日条目（含 fingerprint，便于排查为什么某条进来了）。 */
    @GetMapping("/items")
    ApiResponse<List<ItemView>> items(@RequestParam(value = "date", required = false) String date) {
        LocalDate edition;
        try {
            edition = date == null || date.isBlank()
                    ? LocalDate.now(java.time.ZoneOffset.UTC)
                    : LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "日期格式应为 YYYY-MM-DD");
        }
        return ApiResponse.ok(mapper.findByDate(edition).stream()
                .map(row -> new ItemView(row.id(), row.groupKey(), row.sourceKey(), row.sourceName(),
                        row.title(), row.summary(), row.url(), row.fingerprint(), row.heat(),
                        row.meta(), row.publishedAt(), row.summaryState()))
                .toList());
    }

    /** 删除单条。不存在时 404（不静默成功，否则"删了但还在"无法区分）。 */
    @DeleteMapping("/items/{id}")
    ApiResponse<Boolean> delete(@PathVariable("id") long id) {
        int affected = mapper.deleteById(id);
        if (affected == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "这条速递不存在");
        }
        return ApiResponse.ok(true);
    }

    /** 管理端视图：比学习者端多一个 fingerprint。 */
    record ItemView(long id, String groupKey, String sourceKey, String sourceName, String title,
                    String summary, String url, String fingerprint, Integer heat, String meta,
                    Long publishedAt, String summaryState) {
    }
}
