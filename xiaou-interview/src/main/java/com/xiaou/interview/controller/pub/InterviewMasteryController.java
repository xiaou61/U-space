package com.xiaou.interview.controller.pub;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.interview.domain.InterviewMasteryRecord;
import com.xiaou.interview.dto.HeatmapResponse;
import com.xiaou.interview.dto.MasteryMarkRequest;
import com.xiaou.interview.dto.MasteryResponse;
import com.xiaou.interview.dto.ReviewStatsResponse;
import com.xiaou.interview.service.InterviewMasteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * 面试题掌握度控制器
 *
 * @author xiaou
 */
@Tag(name = "面试题掌握度")
@RestController
@RequestMapping("/interview/mastery")
@RequiredArgsConstructor
public class InterviewMasteryController {

    private final InterviewMasteryService masteryService;

    @Operation(summary = "标记题目掌握度")
    @PostMapping("/mark")
    public Result<MasteryResponse> markMastery(@Validated @RequestBody MasteryMarkRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        MasteryResponse response = masteryService.markMastery(userId, request);
        return Result.success(response);
    }

    @Operation(summary = "获取题目掌握度")
    @GetMapping("/{questionId}")
    public Result<MasteryResponse> getMastery(
            @Parameter(description = "题目ID") @PathVariable Long questionId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        MasteryResponse response = masteryService.getMastery(userId, questionId);
        return Result.success(response);
    }

    @Operation(summary = "批量获取题目掌握度")
    @PostMapping("/batch")
    public Result<Map<Long, MasteryResponse>> batchGetMastery(@RequestBody List<Long> questionIds) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Map<Long, MasteryResponse> result = masteryService.batchGetMastery(userId, questionIds);
        return Result.success(result);
    }

    @Operation(summary = "获取复习统计")
    @GetMapping("/review/stats")
    public Result<ReviewStatsResponse> getReviewStats() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        ReviewStatsResponse response = masteryService.getReviewStats(userId);
        return Result.success(response);
    }

    @Operation(summary = "获取待复习题目列表")
    @GetMapping("/review/list")
    public Result<List<InterviewMasteryRecord>> getReviewList(
            @Parameter(description = "类型：overdue-逾期 today-今日 week-本周 all-全部")
            @RequestParam(defaultValue = "all") String type) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        List<InterviewMasteryRecord> list = masteryService.getReviewList(userId, type);
        return Result.success(list);
    }

    @Operation(summary = "获取学习热力图数据")
    @GetMapping("/heatmap")
    public Result<HeatmapResponse> getHeatmap(
            @Parameter(description = "年份，默认当前年") 
            @RequestParam(required = false) Integer year) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (year == null) {
            year = Year.now().getValue();
        }
        HeatmapResponse response = masteryService.getHeatmap(userId, year);
        return Result.success(response);
    }

    @Operation(summary = "获取某日学习详情")
    @GetMapping("/heatmap/detail")
    public Result<Map<String, Object>> getDayDetail(
            @Parameter(description = "日期，格式：yyyy-MM-dd") @RequestParam String date) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        Map<String, Object> detail = masteryService.getDayDetail(userId, date);
        return Result.success(detail);
    }
}
