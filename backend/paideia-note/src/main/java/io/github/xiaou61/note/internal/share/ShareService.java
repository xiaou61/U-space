package io.github.xiaou61.note.internal.share;

import io.github.xiaou61.note.internal.note.NoteService;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 只读分享的用例。
 *
 * <p>三条纪律：
 * <ul>
 *   <li><b>只有本人能分享自己的笔记</b>：分享前先按 `user_id` 把笔记读一遍，读不到就没有分享。</li>
 *   <li><b>share_id 是 UUID</b>：它是免鉴权端点的唯一凭据，可枚举就等于把所有人的分享摊开。</li>
 *   <li><b>失效一律当不存在</b>：过期、撤回、下架都返回空 {@link Optional}，调用方给同一个 404——
 *       区分开等于告诉访问者"这条分享曾经存在"。</li>
 * </ul>
 */
@Service
public class ShareService {

    /**
     * 分享的固定有效期。
     *
     * <p>ponytail: 固定 30 天，不做成配置、也不做"永久"。理由是这条链接**免鉴权**——
     * 它一旦外泄就是公开的，到期是这个模型唯一的兜底。等真有"长期分享"的需求再谈可配。
     */
    private static final Duration TTL = Duration.ofDays(30);

    private final NoteShareMapper shares;
    private final NoteService notes;

    ShareService(NoteShareMapper shares, NoteService notes) {
        this.shares = shares;
        this.notes = notes;
    }

    /** 对外形状：**只有这三样**，没有作者、没有时间、没有 id。 */
    public record SharedNoteView(String title, String body, Long entryId) {
    }

    @Transactional
    public String share(long userId, long noteId) {
        // 先确认这条笔记是本人的：读不到就没有分享可谈（此时抛 404）
        notes.read(userId, noteId);
        String shareId = UUID.randomUUID().toString();
        shares.insert(shareId, noteId, userId, Instant.now().plus(TTL));
        return shareId;
    }

    /** 某条笔记当前有效的分享 id（本人）。界面据此告诉用户"现在撤的是一条已发出的链接"。 */
    public java.util.List<String> activeShares(long userId, long noteId) {
        // 先确认笔记是本人的：不是本人的笔记连"它有没有被分享过"都不该答
        notes.read(userId, noteId);
        return shares.findActiveForNote(noteId, userId, Instant.now());
    }

    /** 撤回。删不到（不是本人的、已失效）就报 404——同样不确认它是否存在。 */
    @Transactional
    public void revoke(long userId, String shareId) {
        if (shares.revoke(shareId, userId, Instant.now()) == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "分享不存在或已失效");
        }
    }

    /**
     * 读一条有效分享。**免鉴权路径**：这里没有 userId 可校验，安全性全部来自"id 不可枚举"。
     *
     * <p>过期与撤回都在 SQL 里判，所以这里拿到的要么是有效内容、要么什么也没有。
     */
    public Optional<SharedNoteView> read(String shareId) {
        if (shareId == null || shareId.isBlank()) {
            return Optional.empty();
        }
        return shares.findValid(shareId, Instant.now())
                .map(note -> new SharedNoteView(
                        note.getTitle(),
                        note.getBody() == null ? "" : note.getBody(),
                        note.getEntryId()));
    }

    /**
     * 管理员下架（TASK-013 用）。与撤回同一个字段，区别只是不看归属。
     *
     * @return 是否真的改动了（已经是失效状态则返回 false）
     */
    @Transactional
    public boolean revokeByAdmin(String shareId) {
        return shares.revokeByAdmin(shareId, Instant.now()) > 0;
    }
}
