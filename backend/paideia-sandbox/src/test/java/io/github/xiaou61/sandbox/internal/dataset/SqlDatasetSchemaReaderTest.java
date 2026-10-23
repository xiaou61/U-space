package io.github.xiaou61.sandbox.internal.dataset;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.sandbox.internal.run.CodeRun;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 结构 JSON 的生成（WORK-035 T-09）。
 *
 * <p>**输出形状必须与 `scripts/gen-dataset-schema.mjs` 逐字段一致**：前端
 * `dataset-schema.ts` 只认识这一种形状。两处不一致会让"内置集"与"上传集"表现不同，
 * 而这种错在界面上只表现为"侧栏空了"，不会报错。
 */
class SqlDatasetSchemaReaderTest {

    /** 探测查询的真实结果集形状（列顺序来自 `SqlDatasetUploadService.PROBE`）。 */
    private static CodeRun.Table probeTable() {
        return new CodeRun.Table(0, List.of("tbl", "col", "typ", "nullable", "colkey"), List.of(
                List.of("customers", "id", "int", "NO", "PRI"),
                List.of("customers", "name", "varchar(20)", "NO", ""),
                List.of("customers", "note", "varchar(20)", "YES", ""),
                List.of("orders", "id", "int", "NO", "PRI")));
    }

    @Test
    void 生成与构建期脚本一致的形状() {
        String json = SqlDatasetSchemaReader.toJson(probeTable());
        assertThat(json).isNotNull();
        // 顶层两个键，顺序固定
        assertThat(json).startsWith("{\"tables\":[");
        assertThat(json).contains("\"relations\":[]");
        // 表与列
        assertThat(json).contains("\"name\":\"customers\"");
        assertThat(json).contains("\"name\":\"orders\"");
        // 四个字段名与前端 SchemaColumn 一一对应
        assertThat(json).contains("\"type\":\"varchar(20)\"");
        assertThat(json).contains("\"nullable\":true");
        assertThat(json).contains("\"primaryKey\":true");
    }

    /** 主键与可空必须按 MySQL 的取值转换，而不是直接透传字符串。 */
    @Test
    void 主键与可空转成布尔() {
        String json = SqlDatasetSchemaReader.toJson(probeTable());
        // id 是 PRI 且 NOT NULL
        assertThat(json).contains("\"name\":\"id\",\"type\":\"int\",\"nullable\":false,\"primaryKey\":true");
        // note 可空且非主键
        assertThat(json).contains("\"name\":\"note\",\"type\":\"varchar(20)\",\"nullable\":true,\"primaryKey\":false");
    }

    /** 结果集形状不对时返回 null，而不是造一份错的结构。 */
    @Test
    void 列名不匹配时返回null() {
        CodeRun.Table wrong = new CodeRun.Table(0, List.of("a", "b"), List.of(List.of("x", "y")));
        assertThat(SqlDatasetSchemaReader.toJson(wrong)).isNull();
    }

    @Test
    void 空结果集返回null() {
        CodeRun.Table empty = new CodeRun.Table(0, List.of("tbl", "col", "typ", "nullable", "colkey"), List.of());
        assertThat(SqlDatasetSchemaReader.toJson(empty)).isNull();
    }

    @Test
    void 数出表数() {
        String json = SqlDatasetSchemaReader.toJson(probeTable());
        assertThat(SqlDatasetSchemaReader.count(json).tables()).isEqualTo(2);
    }

    @Test
    void 空JSON的表数为0() {
        assertThat(SqlDatasetSchemaReader.count(null).tables()).isZero();
        assertThat(SqlDatasetSchemaReader.count("").tables()).isZero();
    }

    /**
     * 引号与换行必须转义，否则生成的 JSON 解析不了。
     *
     * <p>**不用 Jackson 校验**：本模块没有这个依赖，为一个断言引一个序列化库不值得。
     * 改成两条更直接的断言：转义序列在场，且原始控制字符不在场（后者是"JSON 非法"的真因）。
     */
    @Test
    void 特殊字符被转义() {
        CodeRun.Table tricky = new CodeRun.Table(0, List.of("tbl", "col", "typ", "nullable", "colkey"),
                List.of(List.of("t\"1", "c\\1", "int\n", "NO", "")));
        String json = SqlDatasetSchemaReader.toJson(tricky);
        assertThat(json).isNotNull();
        assertThat(json).as("引号要转义成 \\\"").contains("\\\"");
        assertThat(json).as("反斜杠要转义成 \\\\").contains("\\\\");
        assertThat(json).as("换行要转义成 \\n").contains("\\n");
        // 真换行留在字符串里会让整份 JSON 非法
        assertThat(json).as("不能有裸换行").doesNotContain("\n");
        assertThat(json).as("不能有裸回车").doesNotContain("\r");
    }
}
