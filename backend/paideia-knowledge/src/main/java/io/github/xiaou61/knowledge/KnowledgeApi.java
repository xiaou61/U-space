package io.github.xiaou61.knowledge;

import java.util.Optional;

/**
 * 知识库模块的对外契约。
 *
 * <p><b>只放真实会被别的模块调用的方法。</b>第一个消费者是 AI 精简（WORK-005）：它需要
 * 按 id 取条目正文来生成精简卡片。在此之前这个接口一直是空的——不是遗漏，是按 WORK-004
 * 定下的纪律"不预先设计猜测出来的方法"。
 *
 * <p>新增方法时仍守这条：签名只用包根的类型（如 {@link EntryContent}），实现放
 * {@code internal}，不把内部实体暴露出去。
 */
public interface KnowledgeApi {

    /**
     * 按 id 取**已发布**条目的标题与正文。
     *
     * <p>只给已发布：草稿在学习者侧本来就看不到，让别的模块读到它既没有产品意义，又多一个
     * 越权读取面。不存在与未发布都返回空 {@link Optional}，调用方**无法区分**这两者——
     * 刻意如此，区分开就等于泄露"某个草稿存在"。
     */
    Optional<EntryContent> findPublishedContent(long entryId);
}
