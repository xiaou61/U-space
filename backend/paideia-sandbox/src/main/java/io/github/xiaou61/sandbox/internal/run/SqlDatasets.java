package io.github.xiaou61.sandbox.internal.run;

import io.github.xiaou61.sandbox.internal.dataset.DatasetStorage;
import io.github.xiaou61.sandbox.internal.dataset.SqlDatasetMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * SQL 数据集解析（WORK-032 建，WORK-035 扩成三源）。
 *
 * <p><b>两种来源，一条解析路径</b>：
 * <ol>
 *   <li><b>内置</b>：classpath 里的 `.sql`，受信任内容，任何人可跑，**不依赖对象存储**。
 *       这一档永远在，因为它决定了"对象存储挂了时课程还能不能做题"。</li>
 *   <li><b>私人 / 社区</b>：元信息在 `sql_datasets` 表，内容在 {@link FileStorage}。</li>
 * </ol>
 *
 * <p><b>为什么数据集内容不进数据库</b>：它就是文件。塞成 blob 会让备份与迁移变重，
 * 而对象存储端口（WORK-007）本来就是为这类东西准备的。
 *
 * <p><b>隐藏集的边界</b>：{@link #publicKeys()} 不报隐藏集（否则它进了选择器就等于公开），
 * 但 {@link #sql(long, String)} 仍认它——课程题面里明文写着这个 key，拒绝运行会让关卡做不了。
 */
@Component
public class SqlDatasets {

    /** 内置数据集：key → classpath 资源。加内置集从这里开始。 */
    private static final Map<String, String> REGISTRY = Map.of(
            "orders-public", "orders-public.sql",
            "orders-hidden-a", "orders-hidden-a.sql");

    /**
     * **不下发给客户端的** key。
     *
     * <p>隐藏集的用途是让"把公开集上的答案硬编码进去"失败。它一旦出现在选择器里，
     * 用户选中跑一句 `SELECT *` 就拿到了全部数据，隐藏的意义当场消失——所以能力探测
     * 只报 {@link #publicKeys()}，运行仍然认全部 key（题面本来就明文写着它）。
     */
    private static final Set<String> HIDDEN = Set.of("orders-hidden-a");

    /** 内置集的内容缓存。私人/社区集不缓存内容——它们可以被作者改写。 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /** schema 快照缓存：与内容缓存分开，两者的缺失语义不同（内容缺失是 bug，快照缺失可接受）。 */
    private final Map<String, String> schemaCache = new ConcurrentHashMap<>();

    private final SqlDatasetMapper mapper;
    private final DatasetStorage storage;

    SqlDatasets(SqlDatasetMapper mapper, DatasetStorage storage) {
        this.mapper = mapper;
        this.storage = storage;
    }

    /** 内置集的全部 key（含隐藏集）。给运行与校验用——**不要拿它给客户端渲染选择器**。 */
    public Set<String> keys() {
        return REGISTRY.keySet();
    }

    /** 内置集中可以下发给客户端的 key（不含隐藏集）。顺序固定，避免选择器每次刷新都换位置。 */
    public List<String> publicKeys() {
        return REGISTRY.keySet().stream().filter(key -> !HIDDEN.contains(key)).sorted().toList();
    }

    /** 是否是内置集（受信任档）。 */
    public boolean isBuiltin(String key) {
        return key != null && REGISTRY.containsKey(key);
    }

    /**
     * 取数据集内容（**唯一入口**，必须带 userId）。
     *
     * <p><b>签名里必须有用户</b>：WORK-032 的版本是 `sql(String key)`，没有用户参数——
     * 任何人猜到 key 就能跑别人的私人数据集。这是本项唯一的真安全边界，所以旧签名
     * **直接删除而不是重载**：留一个旧签名，将来一定有人调到它。
     *
     * <p>返回 null 表示"不存在**或**无权访问"，两者**刻意不区分**——区分了就给出一个
     * 枚举 oracle，攻击者能靠文案差异确认某个 key 存在。
     */
    public String sql(long userId, String key) {
        if (key == null) {
            return null;
        }
        // 1. 内置优先：它不依赖数据库与对象存储，任何情况下都可用
        if (isBuiltin(key)) {
            return cache.computeIfAbsent(key, name -> read(REGISTRY.get(name)));
        }
        // 2. 表里的私人 / 社区集
        return mapper.findByKey(key)
                .filter(row -> visible(row, userId))
                .map(row -> storage.read(row.storageId())
                        .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                        .orElse(null))
                .orElse(null);
    }

    /**
     * 可见性判定。
     *
     * <p>社区集必须是 `published`（`draft` 还没发布，`taken_down` 已下架）；
     * 私人集必须是自己拥有。**顺序不重要，但每一条都要在**——漏一条就是越权。
     */
    static boolean visible(SqlDatasetMapper.Row row, long userId) {
        if ("community".equals(row.source())) {
            return "published".equals(row.status());
        }
        if ("private".equals(row.source())) {
            return row.ownerUserId() != null && row.ownerUserId() == userId;
        }
        return false;
    }

    /** 便于测试注入。 */
    Map<String, String> snapshot() {
        Map<String, String> copy = new LinkedHashMap<>();
        for (String key : keys()) {
            copy.put(key, sql(0L, key));
        }
        return copy;
    }

    /**
     * 数据集的结构快照（WORK-035 T-03）。
     *
     * <p>内置集读构建期生成的静态文件；私人/社区集读表里的 `schema_json`。
     * 未知 key、无权访问、或快照缺失时返回 null。
     *
     * <p>**返回原始 JSON 字符串而不是解析成对象**：这份内容会原样转给前端补全与助手工具，
     * 中途转成 Java 结构再序列化一遍只会引入"字段名对不上"这类错。
     */
    public String schema(long userId, String key) {
        if (key == null) {
            return null;
        }
        if (isBuiltin(key)) {
            return schemaCache.computeIfAbsent(key, name -> readOrNull("/sandbox/datasets/" + name + ".schema.json"));
        }
        return mapper.findByKey(key)
                .filter(row -> visible(row, userId))
                .map(SqlDatasetMapper.Row::schemaJson)
                .orElse(null);
    }

    /** 某用户能选的数据集：内置公开集 + 自己的私人集 + 已发布的社区集。 */
    public List<DatasetOption> options(long userId) {
        List<DatasetOption> result = new ArrayList<>();
        for (String key : publicKeys()) {
            result.add(new DatasetOption(key, key, "builtin"));
        }
        for (SqlDatasetMapper.Summary row : mapper.listVisible(userId)) {
            result.add(new DatasetOption(row.datasetKey(), row.name(), row.source()));
        }
        return List.copyOf(result);
    }

    /** 选择器的一项。`source` 让前端分组显示。 */
    public record DatasetOption(String key, String name, String source) {
    }

    /** 读一个 classpath 资源；不存在时返回 null（快照缺失不该让接口 500）。 */
    private static String readOrNull(String path) {
        try (InputStream stream = SqlDatasets.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException cause) {
            throw new UncheckedIOException("读取资源失败：" + path, cause);
        }
    }

    private static String read(String resource) {
        String path = "/sandbox/datasets/" + resource;
        try (InputStream stream = SqlDatasets.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("数据集资源缺失：" + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException cause) {
            throw new UncheckedIOException("读取数据集失败：" + path, cause);
        }
    }
}
