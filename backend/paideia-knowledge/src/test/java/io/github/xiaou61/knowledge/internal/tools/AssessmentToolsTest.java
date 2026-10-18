package io.github.xiaou61.knowledge.internal.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.xiaou61.knowledge.internal.assessment.SelfAssessmentService;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 自评工具的两条纪律：写工具必须标成写（否则它会出现在只读视图里），归属只来自上下文。
 */
class AssessmentToolsTest {

    private static final ToolContext CALLER = ToolContext.internal(7L, "USER");

    private final SelfAssessmentService service = mock(SelfAssessmentService.class);

    @Test
    void listMarksUsesContextUser() {
        when(service.listMine(7L, null)).thenReturn(List.of(
                new SelfAssessmentService.AssessedItem(2L, "索引与回表", "unsure", null)));

        ToolResult result = new AssessmentTools.ListMarks(service)
                .call(CALLER, Map.of("userId", 999L));

        assertThat(result.error()).isFalse();
        assertThat(result.text()).contains("索引与回表", "unsure");
        verify(service).listMine(7L, null);
    }

    @Test
    void markEntryWritesForContextUser() {
        ToolResult result = new AssessmentTools.MarkEntry(service)
                .call(CALLER, Map.of("entryId", 3, "level", "understood", "userId", 999L));

        assertThat(result.error()).isFalse();
        verify(service).mark(7L, 3L, "understood");
    }

    @Test
    void markEntryWithoutLevelFailsBeforeTouchingTheService() {
        ToolResult result = new AssessmentTools.MarkEntry(service).call(CALLER, Map.of("entryId", 3));

        assertThat(result.error()).isTrue();
        verify(service, never()).mark(anyLong(), anyLong(), anyString());
    }

    @Test
    void markEntryReportsBusinessFailureAsToolError() {
        doThrow(new IllegalArgumentException("条目不存在")).when(service).mark(7L, 3L, "understood");

        ToolResult result = new AssessmentTools.MarkEntry(service)
                .call(CALLER, Map.of("entryId", 3, "level", "understood"));

        assertThat(result.error()).isTrue();
        assertThat(result.text()).contains("条目不存在");
    }

    @Test
    void markDescriptorIsWriteTool() {
        assertThat(new AssessmentTools.MarkEntry(service).descriptor().readOnly()).isFalse();
        assertThat(new AssessmentTools.MarkEntry(service).descriptor().scope().key()).isEqualTo("write");
        assertThat(new AssessmentTools.ListMarks(service).descriptor().readOnly()).isTrue();
    }
}
