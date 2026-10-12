package io.github.xiaou61.knowledge;

import java.util.List;
import java.util.Optional;

/**
 * 知识库模块的对外契约。
 *
 * <p><b>只放真实会被别的模块调用的方法。</b>第一个消费者是 AI 精简（WORK-005）：它需要
 * 按 id 取条目正文来生成精简卡片。在此之前这个接口一直是空的——不是遗漏，是按 WORK-004
 * 定下的纪律"不预先设计猜测出来的方法"。第二个消费者是助手运行时（WORK-006）的**只读工具**：
 * 按关键词检索、取分类树、读某个学习者自己的自评标记。三个方法同样是那时才加的。
 *
 * <p>新增方法时仍守这条：签名只用包根的类型，实现放 {@code internal}，不把内部实体暴露出去。
 * 值类型用**嵌套 record** 而不是各开一个文件——它们是这个契约的一部分。
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

    /**
     * 按关键词检索**已发布**条目。
     *
     * <p>同样只给已发布。空关键词返回最近的若干条（助手问"有哪些内容"时不该得到空结果）；
     * 中文单字在 ngram 索引上召回为零，内部按 {@code SearchQuery} 的判定退回 LIKE。
     */
    List<EntryBrief> searchPublished(String keyword, int limit);

    /** 分类的全量扁平列表（带 parentId，层级由调用方自己拼）。只读。 */
    List<CategoryNode> listCategories();

    /**
     * 某个学习者**自己的**自评标记。
     *
     * <p>**必须由调用方传入 userId**，本方法不做别的归属校验——传别人的 id 就会读到别人的标记。
     * 所以助手那侧把它绑在构造期，不让模型有机会提供这个值。
     */
    List<MarkedEntry> findMarks(long userId, int limit);

    /** 检索结果行：只要 id 与标题；正文由 {@link #findPublishedContent} 另取。 */
    record EntryBrief(long id, String title) {
    }

    /** 分类节点。 */
    record CategoryNode(long id, String name, Long parentId) {
    }

    /** 「我标记过的条目」。`level` 懂／还不懂的取值透传，语义不在这里定义。 */
    record MarkedEntry(long entryId, String title, String level) {
    }
}
