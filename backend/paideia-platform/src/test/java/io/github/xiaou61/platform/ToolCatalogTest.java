package io.github.xiaou61.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 目录的校验与过滤链。这一层没有数据库、没有网络，全部是纯判定——所以失败模式都该在这里被钉住。
 *
 * <p>重点覆盖两类"不会在运行时明显报错"的错：清单里混进写工具、过滤顺序不稳定。前者是安全问题，
 * 后者会让客户端缓存与提示词缓存错乱。
 */
class ToolCatalogTest {

    private static final Map<String, Object> OBJECT_SCHEMA = Map.of("type", "object", "properties", Map.of());

    @Test
    void rejectsDuplicateNames() {
        PaideiaTool first = tool("knowledge_search", "knowledge", true, ToolScope.READ, true);
        PaideiaTool second = tool("knowledge_search", "knowledge", true, ToolScope.READ, true);

        assertThatThrownBy(() -> new ToolCatalog(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("knowledge_search")
                .hasMessageContaining("重复");
    }

    @Test
    void rejectsInvalidNames() {
        List<String> invalid = List.of("Search", "knowledge search", "knowledge", "knowledge.", ".search",
                "knowledge_search-now");
        for (String name : invalid) {
            assertThatThrownBy(() -> new ToolCatalog(List.of(tool(name, "knowledge", true, ToolScope.READ, true))))
                    .as("名字 %s 应当被拒绝", name)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("不合法");
        }
    }

    @Test
    void rejectsSchemaThatIsNotAnObject() {
        PaideiaTool broken = new FakeTool(new ToolDescriptor("note_list", "note", "列笔记", "描述",
                Map.of("type", "array"), null, true, false, true, ToolScope.READ, true), ToolResult.ok(""));

        assertThatThrownBy(() -> new ToolCatalog(List.of(broken)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inputSchema");
    }

    @Test
    void rejectsToolsetThatDoesNotMatchNamePrefix() {
        PaideiaTool mismatched = tool("knowledge_search", "note", true, ToolScope.READ, true);

        assertThatThrownBy(() -> new ToolCatalog(List.of(mismatched)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("前缀");
    }

    @Test
    void rejectsReadOnlyToolThatRequiresWriteScope() {
        PaideiaTool contradictory = tool("note_create", "note", true, ToolScope.WRITE, false);

        assertThatThrownBy(() -> new ToolCatalog(List.of(contradictory)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("只读");
    }

    @Test
    void rejectsReadOnlyAndDestructiveTogether() {
        PaideiaTool contradictory = new FakeTool(new ToolDescriptor("note_purge", "note", "清理", "描述",
                OBJECT_SCHEMA, null, true, true, false, ToolScope.READ, false), ToolResult.ok(""));

        assertThatThrownBy(() -> new ToolCatalog(List.of(contradictory)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("矛盾");
    }

    @Test
    void readOnlyViewExcludesWrites() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true),
                tool("note_create", "note", false, ToolScope.WRITE, true)));

        List<ToolDescriptor> view = catalog.view(
                ToolQuery.view(Set.of(), false, null, Set.of(ToolScope.READ, ToolScope.WRITE)));

        assertThat(view).extracting(ToolDescriptor::name).containsExactly("knowledge_search");
    }

    @Test
    void writeViewIncludesWritesOnlyWhenScopeAllows() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true),
                tool("note_create", "note", false, ToolScope.WRITE, true)));

        List<ToolDescriptor> withWrite = catalog.view(
                ToolQuery.view(Set.of(), true, null, Set.of(ToolScope.READ, ToolScope.WRITE)));
        List<ToolDescriptor> readOnlyScope = catalog.view(
                ToolQuery.view(Set.of(), true, null, Set.of(ToolScope.READ)));

        assertThat(withWrite).extracting(ToolDescriptor::name)
                .containsExactly("knowledge_search", "note_create");
        assertThat(readOnlyScope).extracting(ToolDescriptor::name).containsExactly("knowledge_search");
    }

    @Test
    void toolsetFilterKeepsOnlyThatGroup() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true),
                tool("note_list", "note", true, ToolScope.READ, true)));

        assertThat(catalog.view(ToolQuery.view(Set.of("note"), false, null, Set.of(ToolScope.READ))))
                .extracting(ToolDescriptor::name).containsExactly("note_list");
    }

    @Test
    void explicitToolBypassesToolsetFilter() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true),
                tool("note_list", "note", true, ToolScope.READ, true)));

        ToolQuery query = new ToolQuery(Set.of("note"), Set.of("knowledge_search"),
                true, null, Set.of(ToolScope.READ), false);

        assertThat(catalog.view(query)).extracting(ToolDescriptor::name)
                .containsExactly("knowledge_search", "note_list");
    }

    @Test
    void adminScopeRequiresAdminRole() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("admin_stats", "admin", true, ToolScope.ADMIN, false)));

        assertThat(catalog.view(ToolQuery.view(Set.of(), false, "USER", Set.of(ToolScope.ADMIN)))).isEmpty();
        assertThat(catalog.view(ToolQuery.view(Set.of(), false, "ADMIN", Set.of(ToolScope.ADMIN))))
                .extracting(ToolDescriptor::name).containsExactly("admin_stats");
    }

    @Test
    void viewOrderIsDeterministic() {
        List<PaideiaTool> tools = new ArrayList<>();
        // 故意用与工具组/名字相反的注册顺序
        tools.add(tool("note_list", "note", true, ToolScope.READ, true));
        tools.add(tool("knowledge_search", "knowledge", true, ToolScope.READ, true));
        tools.add(tool("knowledge_get_entry", "knowledge", true, ToolScope.READ, true));
        ToolCatalog catalog = new ToolCatalog(tools);

        List<String> first = catalog.view(ToolQuery.view(Set.of(), false, null, Set.of(ToolScope.READ)))
                .stream().map(ToolDescriptor::name).toList();
        List<String> second = catalog.view(ToolQuery.view(Set.of(), false, null, Set.of(ToolScope.READ)))
                .stream().map(ToolDescriptor::name).toList();

        assertThat(first).containsExactly("knowledge_get_entry", "knowledge_search", "note_list");
        assertThat(second).isEqualTo(first);
    }

    @Test
    void defaultsOnlyKeepsCorePack() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true),
                tool("note_purge", "note", false, ToolScope.WRITE, false)));

        assertThat(catalog.view(ToolQuery.core(null))).extracting(ToolDescriptor::name)
                .containsExactly("knowledge_search");
    }

    @Test
    void searchRanksNameHitsAboveDescriptionHits() {
        // 名字里命中检索词 vs 只在描述里命中：前者得分更高，排在前面
        PaideiaTool byName = new FakeTool(new ToolDescriptor("knowledge_recall_entries", "knowledge",
                "按检索条件找条目", "从已发布内容里查找条目", OBJECT_SCHEMA, null, true, false, true,
                ToolScope.READ, true), ToolResult.ok(""));
        PaideiaTool byDescription = new FakeTool(new ToolDescriptor("note_list", "note", "列出笔记",
                "这里也会提到检索两个字", OBJECT_SCHEMA, null, true, false, true,
                ToolScope.READ, true), ToolResult.ok(""));
        ToolCatalog catalog = new ToolCatalog(List.of(byDescription, byName));

        assertThat(catalog.search("检索", 5)).extracting(ToolDescriptor::name)
                .containsExactly("knowledge_recall_entries", "note_list");
    }

    @Test
    void searchReturnsEmptyForUnrelatedQuery() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true)));

        assertThat(catalog.search("区块链", 5)).isEmpty();
        assertThat(catalog.search("   ", 5)).isEmpty();
        assertThat(catalog.search(null, 5)).isEmpty();
    }

    @Test
    void snapshotAndToolsetsExposeRegistration() {
        ToolCatalog catalog = new ToolCatalog(List.of(
                tool("knowledge_search", "knowledge", true, ToolScope.READ, true),
                tool("note_list", "note", true, ToolScope.READ, true)));

        assertThat(catalog.size()).isEqualTo(2);
        assertThat(catalog.toolsets()).containsExactlyInAnyOrder("knowledge", "note");
        assertThat(catalog.snapshot()).extracting(ToolDescriptor::name)
                .containsExactly("knowledge_search", "note_list");
    }

    // ---------------------------------------------------------------- fixtures

    private static PaideiaTool tool(String name, String toolset, boolean readOnly,
                                    ToolScope scope, boolean defaultGroup) {
        ToolDescriptor descriptor = new ToolDescriptor(name, toolset, "标题 " + name,
                "描述 " + name, OBJECT_SCHEMA, null, readOnly, false, true, scope, defaultGroup);
        return new FakeTool(descriptor, ToolResult.ok(name));
    }

    private record FakeTool(ToolDescriptor descriptor, ToolResult result) implements PaideiaTool {

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            return result;
        }
    }
}
