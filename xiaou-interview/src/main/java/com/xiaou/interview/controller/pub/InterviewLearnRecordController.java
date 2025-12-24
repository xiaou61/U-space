package com.xiaou.interview.controller.pub;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.interview.service.InterviewLearnRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 学习记录接口
 *
 * @author xiaou
 */
@Tag(name = "学习记录", description = "面试题学习记录相关接口")
@RestController
@RequestMapping("/interview/learn")
@RequiredArgsConstructor
public class InterviewLearnRecordController {

    private final InterviewLearnRecordService learnRecordService;

    /**
     * 记录学习请求参数
     */
    @Data
    public static class RecordLearnRequest {
        private Long questionSetId;
        private Long questionId;
    }

    /**
     * 学习进度响应
     */
    @Data
    public static class LearnProgressResponse {
        private int learnedCount;
        private List<Long> learnedQuestionIds;

        public LearnProgressResponse(int learnedCount, List<Long> learnedQuestionIds) {
            this.learnedCount = learnedCount;
            this.learnedQuestionIds = learnedQuestionIds;
        }
    }

    @Operation(summary = "记录学习")
    @PostMapping("/record")
    public Result<Void> recordLearn(@RequestBody RecordLearnRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        learnRecordService.recordLearn(userId, request.getQuestionSetId(), request.getQuestionId());
        return Result.success();
    }

    @Operation(summary = "获取题单学习进度")
    @GetMapping("/progress/{questionSetId}")
    public Result<LearnProgressResponse> getProgress(@PathVariable Long questionSetId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.success(new LearnProgressResponse(0, Collections.emptyList()));
        }
        
        int learnedCount = learnRecordService.getLearnedCount(userId, questionSetId);
        List<Long> learnedIds = learnRecordService.getLearnedQuestionIds(userId, questionSetId);
        
        return Result.success(new LearnProgressResponse(learnedCount, learnedIds));
    }

    @Operation(summary = "获取已学习题目ID列表")
    @GetMapping("/questions/{questionSetId}")
    public Result<List<Long>> getLearnedQuestions(@PathVariable Long questionSetId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.success(Collections.emptyList());
        }
        
        List<Long> learnedIds = learnRecordService.getLearnedQuestionIds(userId, questionSetId);
        return Result.success(learnedIds);
    }

    @Operation(summary = "获取用户总学习数量")
    @GetMapping("/total")
    public Result<Integer> getTotalLearned() {
        Long userId = StpUserUtil.getLoginIdAsLong();
        if (userId == null) {
            return Result.success(0);
        }
        
        int total = learnRecordService.getTotalLearnedCount(userId);
        return Result.success(total);
    }
}
