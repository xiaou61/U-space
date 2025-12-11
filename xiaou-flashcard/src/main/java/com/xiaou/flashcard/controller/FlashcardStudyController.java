package com.xiaou.flashcard.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.dto.request.ReviewSubmitRequest;
import com.xiaou.flashcard.dto.request.StudyStartRequest;
import com.xiaou.flashcard.dto.response.ReviewResultResponse;
import com.xiaou.flashcard.dto.response.StudyCardResponse;
import com.xiaou.flashcard.dto.response.TodayStudyResponse;
import com.xiaou.flashcard.service.FlashcardStudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 闪卡学习控制器
 *
 * @author xiaou
 */
@Tag(name = "闪卡学习", description = "闪卡学习相关接口")
@RestController
@RequestMapping("/flashcard/study")
@RequiredArgsConstructor
public class FlashcardStudyController {

    private final FlashcardStudyService studyService;

    @Operation(summary = "获取今日学习任务")
    @GetMapping("/today")
    public Result<TodayStudyResponse> getTodayStudy() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        TodayStudyResponse response = studyService.getTodayStudy(userId);
        return Result.success(response);
    }

    @Operation(summary = "开始学习")
    @PostMapping("/start")
    public Result<StudyCardResponse> startStudy(@RequestBody StudyStartRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        StudyCardResponse response = studyService.startStudy(userId, request);
        if (response == null) {
            return Result.success("今日学习已完成", null);
        }
        return Result.success(response);
    }

    @Operation(summary = "获取下一张闪卡")
    @GetMapping("/next")
    public Result<StudyCardResponse> getNextCard(
            @RequestParam(required = false) Long deckId,
            @RequestParam(required = false, defaultValue = "daily") String mode) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        StudyCardResponse response = studyService.getNextCard(userId, deckId, mode);
        if (response == null) {
            return Result.success("没有更多闪卡了", null);
        }
        return Result.success(response);
    }

    @Operation(summary = "提交复习反馈")
    @PostMapping("/review")
    public Result<ReviewResultResponse> submitReview(@RequestBody ReviewSubmitRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        ReviewResultResponse response = studyService.submitReview(userId, request);
        return Result.success(response);
    }
}
