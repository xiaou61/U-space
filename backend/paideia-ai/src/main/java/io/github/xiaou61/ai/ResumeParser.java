package io.github.xiaou61.ai;


/**
 * 简历解析端口（WORK-018）：一段文本 → 结构化简历。
 *
 * <p>与 {@link EntrySummarizer}、{@link InterviewReviewer} 同一纪律：<b>签名里没有任何框架类型</b>，
 * 端口不做缓存、不管凭据、不做限流、不落库。它只回答"给我文本与凭据，我给你结构"。
 *
 * <p>失败语义：上游不可用、返回空内容、JSON 校验不过、引证对不上原文，一律抛
 * {@link io.github.xiaou61.platform.BizException}；调用方**不得**把失败结果当作简历落库。
 */
public interface ResumeParser {

    /**
     * @param text   抽取出的简历原文（调用方已做过长度截断与"无文字层"判定）
     * @param access 本次调用用的凭据（明文只在这一次调用期间存在）
     */
    Parsed parse(String text, ModelAccess access);

    /**
     * @param data      结构化结果
     * @param dropped   因引证对不上原文而丢弃的条目数
     * @param truncated 因超长/超额被裁剪的字段数
     * @param usage     本次调用的 token 用量；调用方据此结算积分（可能为空，表示上游没给用量）
     */
    record Parsed(ResumeData data, int dropped, int truncated, TokenUsage usage) {
    }
}
