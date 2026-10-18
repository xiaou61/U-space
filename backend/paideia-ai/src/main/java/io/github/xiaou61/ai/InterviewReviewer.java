package io.github.xiaou61.ai;

import io.github.xiaou61.platform.BizException;

/**
 * 面试点评端口（WORK-010 T-10）。
 *
 * <p><b>它是一次独立、不带工具的调用</b>：输入是整场对话的文本，输出是可落库的结构化评分。
 * 这同时绕开 DeepSeek "工具调用与结构化输出不能在同一次调用里并用"那条已知限制——
 * 评分那一次本来就不该带工具。
 *
 * <p>签名里没有任何框架类型（D-09 的要求）；实现放在 {@code internal.provider}。
 * 端口不管凭据、不管计费、不管事务：它只回答"给我这段对话，给我一份评估"。
 *
 * <p>失败语义：上游不可用、返回空、字段不合法，一律抛 {@link BizException}；
 * **调用方不得把失败结果落库**（否则"进度与弱项"会永远错在一次空评分上）。
 */
public interface InterviewReviewer {

    /**
     * @param transcript 整场面试的对话（已渲染成"面试官：… / 候选人：…"的形式）
     * @param access     本次调用用的凭据（明文只在这一次调用期间存在）
     */
    GeneratedReview review(String transcript, ModelAccess access);

    /**
     * 一次点评的结果。
     *
     * @param evaluation 已校验的结构化评分
     * @param usage      用量，供结算；可能为 null（上游没给）
     */
    record GeneratedReview(InterviewEvaluation evaluation, TokenUsage usage) {
    }
}
