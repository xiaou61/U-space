package io.github.xiaou61.ai;

/**
 * 精简端口：把一份条目正文变成一张已校验的结构化卡片。
 *
 * <p><b>签名里没有任何框架类型</b>（D-09 的要求）。实现放在 {@code internal.provider}，
 * 目前由 AgentScope 承载；换实现或升级框架都不动调用方与接口。
 *
 * <p>端口不做缓存、不管凭据、不做限流：它只回答"给我正文与凭据，我给你卡片"。
 * 调用方负责在调用前取凭据、调用后落库。
 *
 * <p>失败语义：上游不可用、被拒、返回空内容、字段校验不通过，一律抛
 * {@link io.github.xiaou61.platform.BizException}；**调用方不得把失败结果写进缓存**。
 */
public interface EntrySummarizer {

    /**
     * @param title  条目标题（进提示词，帮助模型抓住主题）
     * @param body   条目正文（markdown 源码；超长时由实现按段落截断并在结果里标记）
     * @param access 本次调用用的凭据（明文只在这一次调用期间存在）
     */
    GeneratedSummary summarize(String title, String body, ModelAccess access);
}
