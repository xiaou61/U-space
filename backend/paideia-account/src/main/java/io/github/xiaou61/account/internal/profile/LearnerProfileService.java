package io.github.xiaou61.account.internal.profile;

import io.github.xiaou61.account.Identity;
import io.github.xiaou61.account.LearnerProfile;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 画像用例（WORK-010）。
 *
 * <p>两条纪律：
 * <ul>
 *   <li><b>没有行就是默认身份</b>：默认值写在代码里，而不是靠注册时插一行——漏插的后果是
 *       "这个人静默地没有身份"，而界面上看不出来。</li>
 *   <li><b>只接受可选的枚举值</b>：留位的身份（考研/考公/日常学习）不做界面，也不许通过接口
 *       写进来，否则会出现"库里是考研、界面上根本没有这个身份"的幽灵状态。</li>
 * </ul>
 *
 * <p>**身份不参与任何权限判定**（AC-001）：这里不读也不写 `users.role`。
 */
@Service
public class LearnerProfileService {

    private final LearnerProfileMapper mapper;

    LearnerProfileService(LearnerProfileMapper mapper) {
        this.mapper = mapper;
    }

    public LearnerProfile get(long userId) {
        return mapper.find(userId)
                .map(row -> row.toProfile(Identity.ofOrDefault(row.identity())))
                .orElseGet(LearnerProfile::defaults);
    }

    @Transactional
    public LearnerProfile save(long userId, Identity identity, String targetRole,
                               String targetCity, String timeline, String stage) {
        Identity normalized = identity == null ? Identity.JOB_SEEKER : identity;
        if (!normalized.selectable()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "这个身份暂未开放选择（一期只有「" + Identity.JOB_SEEKER.label() + "」）");
        }
        mapper.upsert(userId, normalized.key(), trimToNull(targetRole), trimToNull(targetCity),
                trimToNull(timeline), trimToNull(stage), Instant.now());
        return new LearnerProfile(normalized, trimToNull(targetRole), trimToNull(targetCity),
                trimToNull(timeline), trimToNull(stage), Instant.now());
    }

    /** 注销级联。放在这一层是因为删除语句属于画像域，别让账号的删除逻辑直接拼 SQL。 */
    @Transactional
    public int deleteByUser(long userId) {
        return mapper.deleteByUser(userId);
    }

    /** 空白串归一成 null：前端清空一个输入框应当等于"没有这个值"，而不是存一个空字符串。 */
    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
