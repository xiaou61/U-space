package io.github.xiaou61.knowledge.internal.web;

import io.github.xiaou61.knowledge.internal.track.TrackService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习轨道端点（WORK-024）。
 *
 * <p><b>为什么单独一个前缀 {@code /api/v1/learn}</b>：它不是知识库的视图。知识库是"挑一篇读"，
 * 这里是"沿着一条轨道往下走"。用户明确要求"从零开始不要是知识库"，
 * 所以入口、路径与响应形状都另起一套，而不是给 `/knowledge` 加参数。
 *
 * <p>归属一律取自令牌：进度是本人的，路径里没有 userId。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/learn")
class LearnTrackController {

    private final TrackService tracks;

    LearnTrackController(TrackService tracks) {
        this.tracks = tracks;
    }

    /** 全部轨道 + 我的进度。空轨道也返回，前端显示"待更新"。 */
    @GetMapping("/tracks")
    ApiResponse<List<TrackService.TrackView>> tracks() {
        return ApiResponse.ok(tracks.list(CurrentUserId.require()));
    }

    /**
     * 一条轨道的关卡地图。
     *
     * <p>关卡按章节分组返回，每格带 `done / current / locked` 三种状态与前置 entryId
     * （前端据此画连线）。**不返回正文**：地图页不需要，进关卡时再取。
     */
    @GetMapping("/tracks/{slug}")
    ApiResponse<TrackService.TrackMap> map(@PathVariable("slug") String slug) {
        return ApiResponse.ok(tracks.map(CurrentUserId.require(), slug)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "没有这条学习轨道")));
    }
}
