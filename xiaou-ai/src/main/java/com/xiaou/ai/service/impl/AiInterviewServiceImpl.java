package com.xiaou.ai.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaou.ai.dto.interview.AnswerEvaluationResult;
import com.xiaou.ai.dto.interview.GeneratedQuestion;
import com.xiaou.ai.dto.interview.InterviewSummaryResult;
import com.xiaou.ai.service.AiInterviewService;
import com.xiaou.ai.util.CozeResponseParser;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.enums.CozeWorkflowEnum;
import com.xiaou.common.utils.CozeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 面试AI服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiInterviewServiceImpl implements AiInterviewService {

    private final CozeUtils cozeUtils;

    @Override
    public AnswerEvaluationResult evaluateAnswer(String direction, String level, String style,
                                                  String question, String answer, int followUpCount) {
        try {
            if (!cozeUtils.isClientAvailable()) {
                log.warn("Coze客户端不可用，使用降级评价");
                return AnswerEvaluationResult.fallbackResult(answer, followUpCount);
            }

            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("direction", direction);
            params.put("level", level);
            params.put("style", style);
            params.put("question", question);
            params.put("answer", answer);
            params.put("followUpCount", followUpCount);

            // 调用工作流
            Result<String> result = cozeUtils.runWorkflow(
                    CozeWorkflowEnum.MOCK_INTERVIEW_EVALUATE, params);

            if (!result.isSuccess() || CozeResponseParser.isErrorResponse(result.getData())) {
                log.warn("Coze工作流调用失败: {}", result.getMessage());
                return AnswerEvaluationResult.fallbackResult(answer, followUpCount);
            }

            // 解析结果
            return parseEvaluationResult(result.getData(), answer, followUpCount);

        } catch (Exception e) {
            log.error("AI评价答案失败", e);
            return AnswerEvaluationResult.fallbackResult(answer, followUpCount);
        }
    }

    @Override
    public InterviewSummaryResult generateSummary(String direction, String level,
                                                   int questionCount, int answeredCount, int skippedCount,
                                                   int totalScore, String qaListJson) {
        try {
            if (!cozeUtils.isClientAvailable()) {
                log.warn("Coze客户端不可用，使用降级总结");
                return InterviewSummaryResult.fallbackResult(totalScore);
            }

            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("direction", direction);
            params.put("level", level);
            params.put("questionCount", questionCount);
            params.put("answeredCount", answeredCount);
            params.put("skippedCount", skippedCount);
            params.put("totalScore", totalScore);
            params.put("qaList", qaListJson);

            // 调用工作流
            Result<String> result = cozeUtils.runWorkflow(
                    CozeWorkflowEnum.MOCK_INTERVIEW_SUMMARY, params);

            if (!result.isSuccess() || CozeResponseParser.isErrorResponse(result.getData())) {
                log.warn("Coze工作流调用失败: {}", result.getMessage());
                return InterviewSummaryResult.fallbackResult(totalScore);
            }

            // 解析结果
            return parseSummaryResult(result.getData(), totalScore);

        } catch (Exception e) {
            log.error("生成面试总结失败", e);
            return InterviewSummaryResult.fallbackResult(totalScore);
        }
    }

    @Override
    public List<GeneratedQuestion> generateQuestions(String direction, String level, int count) {
        try {
            if (!cozeUtils.isClientAvailable()) {
                log.warn("Coze客户端不可用，返回空列表");
                return List.of();
            }

            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("direction", direction);
            params.put("level", level);
            params.put("count", count);

            // 调用工作流
            Result<String> result = cozeUtils.runWorkflow(
                    CozeWorkflowEnum.MOCK_INTERVIEW_GENERATE_QUESTIONS, params);

            if (!result.isSuccess() || CozeResponseParser.isErrorResponse(result.getData())) {
                log.warn("Coze工作流调用失败: {}", result.getMessage());
                return List.of();
            }

            // 解析结果
            return parseGeneratedQuestions(result.getData());

        } catch (Exception e) {
            log.error("AI生成题目失败", e);
            return List.of();
        }
    }

    // =================== 私有解析方法 ===================

    private AnswerEvaluationResult parseEvaluationResult(String response, String answer, int followUpCount) {
        JSONObject json = CozeResponseParser.parse(response);
        if (json == null) {
            return AnswerEvaluationResult.fallbackResult(answer, followUpCount);
        }

        AnswerEvaluationResult result = new AnswerEvaluationResult();
        result.setScore(CozeResponseParser.getInt(json, "score", 5));

        // 解析反馈
        JSONObject feedbackJson = json.getJSONObject("feedback");
        if (feedbackJson != null) {
            AnswerEvaluationResult.Feedback feedback = new AnswerEvaluationResult.Feedback();
            feedback.setStrengths(feedbackJson.getBeanList("strengths", String.class));
            feedback.setImprovements(feedbackJson.getBeanList("improvements", String.class));
            result.setFeedback(feedback);
        } else {
            AnswerEvaluationResult.Feedback feedback = new AnswerEvaluationResult.Feedback();
            feedback.setStrengths(List.of());
            feedback.setImprovements(List.of());
            result.setFeedback(feedback);
        }

        result.setNextAction(CozeResponseParser.getString(json, "nextAction", "nextQuestion"));
        result.setFollowUpQuestion(json.getStr("followUpQuestion"));
        result.setReferencePoints(json.getBeanList("referencePoints", String.class));
        result.setFallback(false);

        return result;
    }

    private InterviewSummaryResult parseSummaryResult(String response, int totalScore) {
        JSONObject json = CozeResponseParser.parse(response);
        if (json == null) {
            return InterviewSummaryResult.fallbackResult(totalScore);
        }

        InterviewSummaryResult result = new InterviewSummaryResult();
        result.setSummary(CozeResponseParser.getString(json, "summary", "感谢您完成本次面试"));
        result.setOverallLevel(CozeResponseParser.getString(json, "overallLevel", "良好"));
        result.setSuggestions(json.getBeanList("suggestions", String.class));
        result.setFallback(false);

        return result;
    }

    private List<GeneratedQuestion> parseGeneratedQuestions(String response) {
        List<GeneratedQuestion> questions = new ArrayList<>();

        try {
            // 先尝试解析output包装
            JSONObject wrapper = JSONUtil.parseObj(response);
            String content = response;
            if (wrapper.containsKey("output")) {
                content = wrapper.getStr("output");
            }

            // 解析数组
            JSONArray array = JSONUtil.parseArray(content);
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                GeneratedQuestion question = new GeneratedQuestion();
                question.setQuestion(item.getStr("question"));
                question.setAnswer(item.getStr("answer"));
                question.setKnowledgePoints(item.getStr("knowledgePoints"));
                questions.add(question);
            }
        } catch (Exception e) {
            log.error("解析生成的题目失败: {}", response, e);
        }

        return questions;
    }
}
