package io.github.xiaou61.knowledge.internal;

import io.github.xiaou61.knowledge.EntryContent;
import io.github.xiaou61.knowledge.KnowledgeApi;
import io.github.xiaou61.knowledge.internal.assessment.SelfAssessmentService;
import io.github.xiaou61.knowledge.internal.category.Category;
import io.github.xiaou61.knowledge.internal.category.CategoryService;
import io.github.xiaou61.knowledge.internal.entry.Entry;
import io.github.xiaou61.knowledge.internal.entry.EntryMapper;
import io.github.xiaou61.knowledge.internal.entry.EntryService;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 对外契约的实现。
 *
 * <p>放在 {@code internal} 而不是包根：实现属于内部细节，别的模块只该看到接口。
 *
 * <p>每个方法都**直接复用已有的内部服务／查询**，不另写一套 SQL：ngram 与 LIKE 的判定、
 * 「只给已发布」的过滤、自评的归属，那边都已经有测试盯着；在这里重写一遍等于开出第二个
 * 会漂移的地方。
 */
@Component
class KnowledgeApiImplementation implements KnowledgeApi {

    /** 助手问"有哪些内容"时一次给多少条：够它接着追问，又不至于把上下文塞满。 */
    private static final int DEFAULT_LIMIT = 10;

    private final EntryMapper entryMapper;
    private final EntryService entryService;
    private final CategoryService categoryService;
    private final SelfAssessmentService selfAssessments;

    KnowledgeApiImplementation(EntryMapper entryMapper,
                               EntryService entryService,
                               CategoryService categoryService,
                               SelfAssessmentService selfAssessments) {
        this.entryMapper = entryMapper;
        this.entryService = entryService;
        this.categoryService = categoryService;
        this.selfAssessments = selfAssessments;
    }

    @Override
    public Optional<EntryContent> findPublishedContent(long entryId) {
        Optional<Entry> entry = entryMapper.findPublishedById(entryId);
        // body 可能为 null（别的查询按需只填部分字段），这里取的是带正文的查询；
        // 仍按空串处理，免得把 null 传到提示词拼装里
        return entry.map(it -> new EntryContent(
                it.getId(),
                it.getTitle(),
                it.getBody() == null ? "" : it.getBody(),
                it.getUpdatedAt()));
    }

    @Override
    public List<EntryBrief> searchPublished(String keyword, int limit) {
        // 走既有的分页查询：它内部已经按 SearchQuery 判定全文还是 LIKE，且只返回已发布
        PageResult<EntryService.EntrySummary> page =
                entryService.listPublished(null, keyword, PageQuery.of(1, saneLimit(limit), null));
        return page.items().stream()
                .map(item -> new EntryBrief(item.id(), item.title()))
                .toList();
    }

    @Override
    public List<CategoryNode> listCategories() {
        return categoryService.all().stream()
                .map(KnowledgeApiImplementation::toNode)
                .toList();
    }

    @Override
    public List<MarkedEntry> findMarks(long userId, int limit) {
        // level 传 null = 懂／还不懂两类都要；条数截断在这一层（内部服务按 level 过滤，不按条数）
        return selfAssessments.listMine(userId, null).stream()
                .limit(saneLimit(limit))
                .map(item -> new MarkedEntry(item.entryId(), item.title(), item.level()))
                .toList();
    }

    /**
     * 上限的兜底。
     *
     * <p>调用方来自模型（工具参数），所以这里的正数校验是**信任边界**：模型可能给 0 或负数，
     * 直接透给分页组件会得到奇怪结果。上限 50 也是防它一次要一千条把上下文塞爆。
     */
    private static int saneLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, 50);
    }

    private static CategoryNode toNode(Category category) {
        return new CategoryNode(category.getId(), category.getName(), category.getParentId());
    }
}
