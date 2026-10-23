package io.github.xiaou61.sandbox.internal.user;

import io.github.xiaou61.platform.UserDeletedEvent;
import io.github.xiaou61.sandbox.internal.audit.CodeRunMapper;
import io.github.xiaou61.sandbox.internal.dataset.SqlDatasetMapper;
import java.time.Clock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 注销时删掉这个人的运行审计与数据集（WORK-015 / WORK-035）。
 *
 * <p><b>为什么审计也删</b>：项目口径是"注销即级联硬删"（WORK-006 起，WORK-009 把积分流水也按这条办了）。
 * 运行记录虽然是排查材料，但它带 `user_id`，留一行等于"这个人还在这里出现过"。
 * 代价是注销用户的运行历史不再完整——这是明确选过的方向，别改成"保留审计"。
 *
 * <p><b>数据集分两种处理</b>（WORK-035）：
 * <ul>
 *   <li>私人集**删掉**——它只有本人能看，留着没有意义；</li>
 *   <li>社区集**不删，只解除归属**——它已被主动发布，别人可能正在引用。删掉会让别人的练习
 *       突然失效，而这与"删除本人数据"不是同一件事：它已经不是私有数据了。</li>
 * </ul>
 *
 * <p>`BEFORE_COMMIT`：与账号删除在同一个事务里，不出"人没了记录还在"的中间态。
 */
@Component
class SandboxUserDeletionListener {

    private final CodeRunMapper mapper;
    private final SqlDatasetMapper datasets;
    private final Clock clock;

    SandboxUserDeletionListener(CodeRunMapper mapper, SqlDatasetMapper datasets, Clock clock) {
        this.mapper = mapper;
        this.datasets = datasets;
        this.clock = clock;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onUserDeleted(UserDeletedEvent event) {
        mapper.deleteByUser(event.userId());
        datasets.deletePrivateByOwner(event.userId());
        datasets.orphanCommunityByOwner(event.userId(), clock.instant());
        datasets.deleteReportsByReporter(event.userId());
    }
}
