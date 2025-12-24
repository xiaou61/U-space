package com.xiaou.interview.service;

import com.xiaou.interview.domain.InterviewMasteryRecord;
import com.xiaou.interview.dto.HeatmapResponse;
import com.xiaou.interview.dto.MasteryMarkRequest;
import com.xiaou.interview.dto.MasteryResponse;
import com.xiaou.interview.dto.ReviewStatsResponse;

import java.util.List;
import java.util.Map;

/**
 * 面试题掌握度服务接口
 *
 * @author xiaou
 */
public interface InterviewMasteryService {

    /**
     * 标记题目掌握度
     *
     * @param userId  用户ID
     * @param request 标记请求
     * @return 掌握度响应
     */
    MasteryResponse markMastery(Long userId, MasteryMarkRequest request);

    /**
     * 获取题目掌握度
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 掌握度响应
     */
    MasteryResponse getMastery(Long userId, Long questionId);

    /**
     * 批量获取题目掌握度
     *
     * @param userId      用户ID
     * @param questionIds 题目ID列表
     * @return 掌握度Map，key为题目ID
     */
    Map<Long, MasteryResponse> batchGetMastery(Long userId, List<Long> questionIds);

    /**
     * 获取复习统计
     *
     * @param userId 用户ID
     * @return 复习统计响应
     */
    ReviewStatsResponse getReviewStats(Long userId);

    /**
     * 获取待复习题目列表
     *
     * @param userId 用户ID
     * @param type   类型：overdue-逾期 today-今日 week-本周 all-全部
     * @return 待复习题目列表
     */
    List<InterviewMasteryRecord> getReviewList(Long userId, String type);

    /**
     * 获取学习热力图数据
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 热力图响应
     */
    HeatmapResponse getHeatmap(Long userId, int year);

    /**
     * 获取某日学习详情
     *
     * @param userId 用户ID
     * @param date   日期（格式：yyyy-MM-dd）
     * @return 当日学习详情
     */
    Map<String, Object> getDayDetail(Long userId, String date);
}
