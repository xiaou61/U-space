package io.github.xiaou61.account.internal.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.xiaou61.account.AccountApi;
import io.github.xiaou61.account.Identity;
import io.github.xiaou61.account.LearnerProfile;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolResult;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AccountToolsTest {

    private static final ToolContext CALLER = ToolContext.internal(7L, "USER");

    private final AccountApi accounts = mock(AccountApi.class);

    @Test
    void meReadsProfileOfContextUser() {
        when(accounts.findProfile("7")).thenReturn(Optional.of(new LearnerProfile(
                Identity.JOB_SEEKER, "后端开发", "杭州", "秋招", "面试", null)));

        ToolResult result = new AccountTools.Me(accounts).call(CALLER, Map.of("userId", 999L));

        assertThat(result.error()).isFalse();
        assertThat(result.text()).contains("找工作", "后端开发", "杭州", "秋招", "面试");
    }

    @Test
    void missingAccountIsAToolError() {
        when(accounts.findProfile("7")).thenReturn(Optional.empty());

        ToolResult result = new AccountTools.Me(accounts).call(CALLER, Map.of());

        assertThat(result.error()).isTrue();
        assertThat(result.text()).contains("没有找到");
    }

    @Test
    void descriptorExposesNoSensitiveFields() {
        assertThat(new AccountTools.Me(accounts).descriptor().inputSchema())
                .containsEntry("properties", Map.of());
        assertThat(new AccountTools.Me(accounts).descriptor().readOnly()).isTrue();
    }
}
