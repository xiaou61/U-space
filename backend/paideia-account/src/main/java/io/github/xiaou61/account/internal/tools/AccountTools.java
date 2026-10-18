package io.github.xiaou61.account.internal.tools;

import io.github.xiaou61.account.AccountApi;
import io.github.xiaou61.account.LearnerProfile;
import io.github.xiaou61.platform.PaideiaTool;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolDescriptor;
import io.github.xiaou61.platform.ToolResult;
import io.github.xiaou61.platform.ToolScope;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 账号工具（WORK-019）：只暴露"我是谁、我的目标是什么"。
 *
 * <p><b>为什么只给这一点</b>：外部助手需要知道调用者是谁才能说对话（"你在准备找工作" vs
 * "你在日常学习"），但它不需要邮箱、注册时间、登录历史这类东西。契约暴露得越少，
 * 越不需要为将来某次意外泄露解释。
 *
 * <p>**不含 role**：角色是权限轴，外部助手知道它只会诱导模型去"试着做管理操作"。
 */
public final class AccountTools {

    private AccountTools() {
    }

    @Component
    public static class Me implements PaideiaTool {

        private final AccountApi accounts;

        Me(AccountApi accounts) {
            this.accounts = accounts;
        }

        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor(
                    "account.me", "account", "读取当前账号的身份与目标",
                    "读取**当前调用者自己**的身份（如「找工作」）与目标岗位/城市/投递阶段。"
                            + "不含邮箱、密码、角色等敏感字段。用户问「我是谁」「我的目标是什么」时用它。",
                    Map.of("type", "object", "properties", Map.of(), "required", List.of()),
                    null, true, false, true, ToolScope.READ, true);
        }

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            // 契约以字符串 userId 表达（与令牌 sub 一致）；这里不吞掉非法值，而是当作失败
            Optional<LearnerProfile> profile = accounts.findProfile(String.valueOf(context.userId()));
            if (profile.isEmpty()) {
                return ToolResult.fail("没有找到当前账号");
            }
            LearnerProfile value = profile.get();
            StringBuilder text = new StringBuilder("身份：" + value.identity().label());
            append(text, "目标岗位", value.targetRole());
            append(text, "目标城市", value.targetCity());
            append(text, "招聘周期", value.timeline());
            append(text, "当前阶段", value.stage());
            return ToolResult.ok(text.toString());
        }

        private static void append(StringBuilder text, String label, String value) {
            if (value != null && !value.isBlank()) {
                text.append('；').append(label).append('：').append(value);
            }
        }
    }
}
