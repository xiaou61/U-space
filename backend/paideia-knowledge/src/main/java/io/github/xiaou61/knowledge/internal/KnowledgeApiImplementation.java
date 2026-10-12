package io.github.xiaou61.knowledge.internal;

import io.github.xiaou61.knowledge.EntryContent;
import io.github.xiaou61.knowledge.KnowledgeApi;
import io.github.xiaou61.knowledge.internal.entry.Entry;
import io.github.xiaou61.knowledge.internal.entry.EntryMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 对外契约的实现。
 *
 * <p>放在 {@code internal} 而不是包根：实现属于内部细节，别的模块只该看到接口。
 *
 * <p>直接用 {@code EntryMapper.findPublishedById}，不再往 {@code EntryService} 上叠一层方法：
 * 那个查询本来就写着 {@code status = 'published'}，正是这里要的语义；多包一层只是多一处
 * 可以写错的地方。
 */
@Component
class KnowledgeApiImplementation implements KnowledgeApi {

    private final EntryMapper entryMapper;

    KnowledgeApiImplementation(EntryMapper entryMapper) {
        this.entryMapper = entryMapper;
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
}
