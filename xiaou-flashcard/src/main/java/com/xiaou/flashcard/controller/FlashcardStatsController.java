package com.xiaou.flashcard.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.flashcard.domain.FlashcardDailyStats;
import com.xiaou.flashcard.dto.response.StatsOverviewResponse;
import com.xiaou.flashcard.service.FlashcardStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 闪卡统计控制器
 *
 * @author xiaou
 */
@Tag(name = "闪卡统计", description = "闪卡学习统计接口")
@RestController
@RequestMapping("/flashcard/stats")
@RequiredArgsConstructor
public class FlashcardStatsController {

    private final FlashcardStatsService statsService;

    @Operation(summary = "获取学习统计概览")
    @GetMapping("/overview")
    public Result<StatsOverviewResponse> getOverview() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        StatsOverviewResponse response = statsService.getOverview(userId);
        return Result.success(response);
    }

    @Operation(summary = "获取每日学习趋势")
    @GetMapping("/daily")
    public Result<List<FlashcardDailyStats>> getDailyTrend(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        List<FlashcardDailyStats> stats = statsService.getDailyTrend(userId, days);
        return Result.success(stats);
    }

    @Operation(summary = "获取连续学习天数")
    @GetMapping("/streak")
    public Result<Integer> getStreakDays() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        int streak = statsService.getStreakDays(userId);
        return Result.success(streak);
    }
}
