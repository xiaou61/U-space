package io.github.xiaou61.note.internal.note;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 笔记的用例。
 *
 * <p>三条纪律：
 * <ul>
 *   <li><b>user_id 只从调用方（令牌）来</b>，这里不接受任何"代表别人"的参数。</li>
 *   <li><b>读不到一律当不存在</b>：拿不到就不返回，调用方给 404——不区分"没有"与"不是你的"。</li>
 *   <li><b>长度校验在写入前</b>：它是信任边界（将来的写入方包含助手），超长要报错而不是让
 *       数据库报一句用户看不懂的话。</li>
 * </ul>
 */
@Service
public class NoteService {

    /** 与迁移里的列宽一致。 */
    private static final int MAX_TITLE_CHARS = 200;
    /**
     * 正文上限。
     *
     * <p>MEDIUMTEXT 能装 16MB，但那是列的上限不是产品上限——一份笔记几十万字说明该拆成多条了，
     * 而没上限意味着一次请求可以把库写满。ponytail: 固定 20000，等真有长笔记需求再提。
     */
    private static final int MAX_BODY_CHARS = 20_000;

    /** 列表一次最多给多少条。ponytail: 固定值，等真有人攒到那么多再做分页。 */
    private static final int LIST_LIMIT = 200;

    private final NoteMapper mapper;

    NoteService(NoteMapper mapper) {
        this.mapper = mapper;
    }

    /** 对外形状。**不含 userId**——界面不需要，带上只是多一个会漏出去的字段。 */
    public record NoteView(Long id, Long entryId, String title, String body, Instant createdAt, Instant updatedAt) {
    }

    /** 列表项：不带正文，带一段截断的摘要（列表页用）。 */
    public record NoteSummary(Long id, Long entryId, String title, String excerpt, Instant updatedAt) {
    }

    @Transactional
    public NoteView create(long userId, Long entryId, String title, String body) {
        Note note = new Note();
        note.setUserId(userId);
        note.setEntryId(entryId);
        note.setTitle(requireText(title, "标题", MAX_TITLE_CHARS));
        note.setBody(requireBody(body));
        mapper.insert(note);
        return toView(mapper.findByIdAndUser(note.getId(), userId).orElseThrow(NoteService::notFound));
    }

    public List<NoteSummary> list(long userId) {
        return mapper.listByUser(userId, LIST_LIMIT).stream()
                .map(note -> new NoteSummary(note.getId(), note.getEntryId(), note.getTitle(), note.getExcerpt(), note.getUpdatedAt()))
                .toList();
    }

    public NoteView read(long userId, long id) {
        return toView(mapper.findByIdAndUser(id, userId).orElseThrow(NoteService::notFound));
    }

    @Transactional
    public NoteView update(long userId, long id, Long entryId, String title, String body) {
        Note note = mapper.findByIdAndUser(id, userId).orElseThrow(NoteService::notFound);
        note.setEntryId(entryId);
        note.setTitle(requireText(title, "标题", MAX_TITLE_CHARS));
        note.setBody(requireBody(body));
        mapper.update(note);
        return toView(mapper.findByIdAndUser(id, userId).orElseThrow(NoteService::notFound));
    }

    @Transactional
    public void delete(long userId, long id) {
        if (mapper.deleteByIdAndUser(id, userId) == 0) {
            throw notFound();
        }
    }

    /** 供别处（如注销流程）判断笔记是否存在而不用抛异常。 */
    public Optional<NoteView> find(long userId, long id) {
        return mapper.findByIdAndUser(id, userId).map(NoteService::toView);
    }

    private static NoteView toView(Note note) {
        return new NoteView(note.getId(), note.getEntryId(), note.getTitle(),
                note.getBody() == null ? "" : note.getBody(), note.getCreatedAt(), note.getUpdatedAt());
    }

    private static String requireText(String raw, String label, int maxChars) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, label + "不能为空");
        }
        if (trimmed.length() > maxChars) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, label + "不能超过 " + maxChars + " 个字符");
        }
        return trimmed;
    }

    /** 正文允许为空串（先建个空壳再写内容），但**不允许超长**。 */
    private static String requireBody(String raw) {
        String body = raw == null ? "" : raw;
        if (body.length() > MAX_BODY_CHARS) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "正文不能超过 " + MAX_BODY_CHARS + " 个字符");
        }
        return body;
    }

    private static BizException notFound() {
        return new BizException(ErrorCode.NOT_FOUND, "笔记不存在");
    }
}
