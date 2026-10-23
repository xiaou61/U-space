package io.github.xiaou61.sandbox.internal.run;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * SQL 数据集白名单（WORK-032）。
 *
 * <p><b>数据集为什么在服务端</b>：隐藏数据集的用途是让"把公开集上的答案硬编码进去"失败。
 * 如果数据集由客户端上传，客户端就必然持有它，隐藏集也就不隐藏了。所以数据集放在
 * 沙箱模块的 resources 里，客户端**只传 key**，服务端按 key 取内容。
 *
 * <p>顺带解决另一件事：客户端上行的 SQL 永远不会被当成"数据集"执行，于是
 * "数据集必须先于用户 SQL 生效"这条顺序由服务端保证，不依赖客户端配合。
 *
 * <p>数据集是**受信任内容**（作者写的建表 + 插数），不是用户输入。加数据集 = 往
 * `resources/sandbox/datasets/` 放一个 `.sql` 并在下面登记 key。
 */
@Component
public class SqlDatasets {

    /** key → classpath 资源。加数据集从这里开始。 */
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

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /** schema 快照缓存：与内容缓存分开，两者的缺失语义不同（内容缺失是 bug，快照缺失可接受）。 */
    private final Map<String, String> schemaCache = new ConcurrentHashMap<>();

    /** 已知的 key。给运行与校验用——**不要拿它给客户端渲染选择器**。 */
    public Set<String> keys() {
        return REGISTRY.keySet();
    }

    /** 可以下发给客户端的 key（不含隐藏集）。顺序固定，避免选择器每次刷新都换位置。 */
    public List<String> publicKeys() {
        return REGISTRY.keySet().stream().filter(key -> !HIDDEN.contains(key)).sorted().toList();
    }

    public boolean exists(String key) {
        return key != null && REGISTRY.containsKey(key);
    }

    /**
     * 取数据集内容。未知 key 返回 null——调用方负责拒绝，**不要在这里抛异常**：
     * 未知 key 是客户端错误（400），不是服务端故障（500）。
     */
    public String sql(String key) {
        if (!exists(key)) {
            return null;
        }
        return cache.computeIfAbsent(key, name -> read(REGISTRY.get(name)));
    }

    /** 便于测试注入。 */
    Map<String, String> snapshot() {
        Map<String, String> copy = new LinkedHashMap<>();
        for (String key : keys()) {
            copy.put(key, sql(key));
        }
        return copy;
    }

    /**
     * 数据集的结构快照（WORK-035 T-03）。未知 key 或快照文件缺失时返回 null。
     *
     * <p><b>快照是构建期生成的静态文件</b>（`scripts/gen-dataset-schema.mjs`），不在运行时抓：
     * 抓一次要起容器（3–5 秒 + 一次配额），而数据集内容不变时 schema 也不会变。
     *
     * <p>**返回原始 JSON 字符串而不是解析成对象**：这份内容会原样转给前端补全与助手工具，
     * 中途转成 Java 结构再序列化一遍只会引入"字段名对不上"这类错，而不带来任何好处。
     */
    public String schema(String key) {
        if (!exists(key)) {
            return null;
        }
        return schemaCache.computeIfAbsent(key, name -> readOrNull("/sandbox/datasets/" + name + ".schema.json"));
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
