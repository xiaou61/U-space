package io.github.xiaou61.sandbox.internal.user;

import io.github.xiaou61.platform.UserDeletedEvent;
import io.github.xiaou61.sandbox.internal.audit.CodeRunMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 注销时删掉这个人的运行审计（WORK-015）。
 *
 * <p><b>为什么审计也删</b>：项目口径是"注销即级联硬删"（WORK-006 起，WORK-009 把积分流水也按这条办了）。
 * 运行记录虽然是排查材料，但它带 `user_id`，留一行等于"这个人还在这里出现过"。
 * 代价是注销用户的运行历史不再完整——这是明确选过的方向，别改成"保留审计"。
 *
 * <p>`BEFORE_COMMIT`：与账号删除在同一个事务里，不出"人没了记录还在"的中间态。
 */
@Component
class SandboxUserDeletionListener {

    private final CodeRunMapper mapper;

    SandboxUserDeletionListener(CodeRunMapper mapper) {
        this.mapper = mapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onUserDeleted(UserDeletedEvent event) {
        mapper.deleteByUser(event.userId());
    }
}
