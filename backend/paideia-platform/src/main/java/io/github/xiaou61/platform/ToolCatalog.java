package io.github.xiaou61.platform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * 平台工具目录：**唯一真源**（WORK-019 REQ-001）。
 *
 * <p>内部助手与外部 MCP 都从这里取清单，所以"内部有、外部另写一份"这种分叉在结构上不可能发生。
 * 它刻意是**普通类而不是 Spring 组件**：`paideia-platform` 是纯契约库，不引 Spring；Bean 由
 * 装配根（`paideia-app`）声明，两个适配器按类型注入。
 *
 * <p><b>校验在构造期完成**：重名、非法名、schema 不是 object、只读标记自相矛盾——任何一条都要
 * 让应用**起不来**并点名具体工具，而不是等到某个视图里出现一条坏工具。启动失败比线上静默少一个
 * 工具好排查得多。
 *
 * <p><b>过滤链只有一条</b>：{@link #view(ToolQuery)} 是内外共用的那一条。顺序写死并在此说明，
 * 因为"哪条规则先生效"如果分散到两个适配器，迟早会分叉。
 */
public final class ToolCatalog {

    /** 确定性排序：先工具组、再工具名。提示词缓存与客户端缓存都靠它。 */
    private static final Comparator<ToolDescriptor> ORDER =
            Comparator.comparing(ToolDescriptor::toolset).thenComparing(ToolDescriptor::name);

    private final Map<String, PaideiaTool> byName;
    private final Set<String> toolsets;

    public ToolCatalog(List<PaideiaTool> tools) {
        Map<String, PaideiaTool> registered = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        for (PaideiaTool tool : tools == null ? List.<PaideiaTool>of() : tools) {
            ToolDescriptor descriptor = tool == null ? null : tool.descriptor();
            if (descriptor == null) {
                problems.add("有一个工具没有描述符（null）");
                continue;
            }
            String name = descriptor.name();
            if (!ToolNaming.isValid(name)) {
                problems.add("工具名不合法：" + name + "（要求 toolset.verb_object 形状，小写字母、数字、下划线，最多 "
                        + ToolNaming.MAX_LENGTH + " 字符）");
                continue;
            }
            if (registered.containsKey(name)) {
                problems.add("工具名重复：" + name + "（同一名称注册了两次，会互相覆盖）");
                continue;
            }
            validateDescriptor(descriptor, problems);
            registered.put(name, tool);
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("工具目录校验失败：" + String.join("；", problems));
        }
        this.byName = Map.copyOf(registered);
        Set<String> groups = new TreeSet<>();
        registered.values().forEach(tool -> groups.add(tool.descriptor().toolset()));
        this.toolsets = Set.copyOf(groups);
    }

    /**
     * 描述符自身的自洽性。
     *
     * <p>这几条都是"写的人当时想错了"才会出现，而且都不会在运行时暴露成明显错误——只会在某天
     * 表现为"只读视图里能删数据"或"模型拿到的 schema 是 null"。所以放在启动期。
     */
    private static void validateDescriptor(ToolDescriptor descriptor, List<String> problems) {
        String name = descriptor.name();
        Object type = descriptor.inputSchema() == null ? null : descriptor.inputSchema().get("type");
        if (!"object".equals(type)) {
            problems.add("工具 " + name + " 的 inputSchema 必须是 type=object（当前：" + type + "）");
        }
        if (descriptor.description() == null || descriptor.description().isBlank()) {
            problems.add("工具 " + name + " 缺少描述（模型选不选得对主要靠它）");
        }
        if (descriptor.scope() == null) {
            problems.add("工具 " + name + " 没有声明 scope");
        }
        if (descriptor.readOnly() && descriptor.scope() == ToolScope.WRITE) {
            problems.add("工具 " + name + " 标了只读却要求 write scope");
        }
        if (descriptor.readOnly() && descriptor.destructive()) {
            problems.add("工具 " + name + " 同时标了只读与破坏性，二者矛盾");
        }
        // 工具组必须与名字前缀一致：否则视图按组切分时会切出"名字说 knowledge、实际在 note 组"的工具，
        // 外部客户端看名字猜组会一直猜错。
        String prefix = ToolNaming.toolsetOf(name);
        if (prefix != null && !prefix.equals(descriptor.toolset())) {
            problems.add("工具 " + name + " 的名字前缀是 " + prefix + "，但声明的工具组是 "
                    + descriptor.toolset() + "（两者必须一致）");
        }
    }

    /**
     * 按查询过滤，返回**确定性有序**的描述符列表。
     *
     * <p>过滤顺序（固定）：
     * <ol>
     *   <li>只读：`readOnly=true` 时排除写工具；</li>
     *   <li>工具组 / 显式工具：显式列出的工具**越过**工具组过滤（`toolsets` 为空表示不过滤组）；</li>
     *   <li>默认组：`defaultsOnly=true` 时只留 `defaultGroup`；</li>
     *   <li>scope：工具所需 scope 不在凭据里即排除；</li>
     *   <li>角色：`ADMIN` scope 的工具额外要求 `ADMIN` 角色。</li>
     * </ol>
     * 最后按工具组、工具名排序。
     */
    public List<ToolDescriptor> view(ToolQuery query) {
        return byName.values().stream()
                .map(PaideiaTool::descriptor)
                .filter(descriptor -> !query.readOnly() || descriptor.readOnly())
                .filter(descriptor -> query.toolsets().isEmpty()
                        || query.toolsets().contains(descriptor.toolset())
                        || query.tools().contains(descriptor.name()))
                .filter(descriptor -> !query.defaultsOnly() || descriptor.defaultGroup())
                .filter(descriptor -> query.scopes().contains(descriptor.scope()))
                .filter(descriptor -> descriptor.scope() != ToolScope.ADMIN
                        || "ADMIN".equalsIgnoreCase(query.role()))
                .sorted(ORDER)
                .toList();
    }

    /** 按名字取工具。调用方必须先用 {@link #view} 确认它在当前视图里——这里不重复鉴权。 */
    public Optional<PaideiaTool> find(String name) {
        return name == null ? Optional.empty() : Optional.ofNullable(byName.get(name));
    }

    /** 全部已注册工具（确定性顺序）。用于导出文档与快照测试。 */
    public List<ToolDescriptor> snapshot() {
        return byName.values().stream().map(PaideiaTool::descriptor).sorted(ORDER).toList();
    }

    /** 全部已注册的工具组。 */
    public Set<String> toolsets() {
        return toolsets;
    }

    /** 工具总数。内部适配器据它决定要不要启用 `find_tools`。 */
    public int size() {
        return byName.size();
    }

    /**
     * 词面检索（内部 `find_tools` 用，REQ-007）。
     *
     * <p>**不调模型、不走向量**：项目已裁"本期不做向量化检索"。打分是名称命中 3、标题 2、
     * 描述 1、工具组 1，多个词相加；命中数相同则按目录顺序（确定性）。
     *
     * <p>中文没有空格，整串当一个词处理即可——工具描述是短文本，子串匹配已经够用。
     */
    public List<ToolDescriptor> search(String query, int limit) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<String> terms = List.of(normalized.split("\\s+"));
        int capped = Math.max(1, Math.min(limit, 50));
        record Scored(ToolDescriptor descriptor, int score) {
        }
        return byName.values().stream()
                .map(PaideiaTool::descriptor)
                .map(descriptor -> new Scored(descriptor, score(descriptor, terms)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(Scored::score).reversed()
                        .thenComparing(scored -> scored.descriptor(), ORDER))
                .limit(capped)
                .map(Scored::descriptor)
                .toList();
    }

    private static int score(ToolDescriptor descriptor, List<String> terms) {
        String name = descriptor.name().toLowerCase(Locale.ROOT);
        String title = descriptor.title() == null ? "" : descriptor.title().toLowerCase(Locale.ROOT);
        String description = descriptor.description().toLowerCase(Locale.ROOT);
        String toolset = descriptor.toolset().toLowerCase(Locale.ROOT);
        int total = 0;
        for (String term : terms) {
            if (term.isEmpty()) {
                continue;
            }
            if (name.contains(term)) {
                total += 3;
            }
            if (title.contains(term)) {
                total += 2;
            }
            if (description.contains(term)) {
                total += 1;
            }
            if (toolset.contains(term)) {
                total += 1;
            }
        }
        return total;
    }
}
