package com.xiaou.flashcard.controller.user;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.dto.request.FlashcardStudySubmitRequest;
import com.xiaou.flashcard.dto.response.FlashcardHeatmapVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyResultVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyStatsVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyVO;
import com.xiaou.flashcard.service.FlashcardStudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡学习控制器（用户端）
 *
 * @author xiaou
 */
@Tag(name = "闪卡学习")
@RestController
@RequestMapping("/flashcard/study")
@RequiredArgsConstructor
public class FlashcardStudyController {

    private final FlashcardStudyService studyService;

    @Operation(summary = "获取今日待学习的卡片")
    @GetMapping("/today")
    public Result<List<FlashcardStudyVO>> getTodayStudyCards(
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        List<FlashcardStudyVO> cards = studyService.getTodayStudyCards(userId, limit);
        return Result.success(cards);
    }

    @Operation(summary = "获取指定卡组的下一张待学习卡片")
    @GetMapping("/deck/{deckId}/next")
    public Result<FlashcardStudyVO> getNextCard(@PathVariable Long deckId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        FlashcardStudyVO card = studyService.getNextCard(deckId, userId);
        return Result.success(card);
    }

    @Operation(summary = "提交学习结果")
    @PostMapping("/submit")
    public Result<FlashcardStudyResultVO> submitStudyResult(@Valid @RequestBody FlashcardStudySubmitRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        FlashcardStudyResultVO result = studyService.submitStudyResult(request, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取学习统计")
    @GetMapping("/stats")
    public Result<FlashcardStudyStatsVO> getStudyStats(
            @RequestParam(required = false) Long deckId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        FlashcardStudyStatsVO stats = studyService.getStudyStats(userId, deckId);
        return Result.success(stats);
    }

    @Operation(summary = "获取学习热力图数据")
    @GetMapping("/heatmap")
    public Result<FlashcardHeatmapVO> getHeatmap(
            @RequestParam(required = false, defaultValue = "365") Integer days) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        FlashcardHeatmapVO heatmap = studyService.getHeatmap(userId, days);
        return Result.success(heatmap);
    }
}
