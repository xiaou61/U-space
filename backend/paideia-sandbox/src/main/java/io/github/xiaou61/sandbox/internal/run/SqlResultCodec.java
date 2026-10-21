package io.github.xiaou61.sandbox.internal.run;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把 `mysql --xml` 的输出解析成结果集（WORK-032）。
 *
 * <p><b>为什么是 XML 而不是 `--batch`</b>：真跑过才看清差距。`--batch --raw` 下多个结果集
 * 顺序拼在 stdout 上、**没有任何边界**；我曾用"语句之间插标记查询"来切，但标记行是单列表头 `m`
 * 加一行值，而用户的真实结果也可能恰好是那个形状，`--raw` 又无法转义，于是**切错而且不报错**
 * （两条测试当场抓到）。换 `--xml` 后每个结果集自带 `<resultset statement="...">` 起止，
 * `xsi:nil` 表达 NULL，DECIMAL 保留尾零，三件事一次解决。
 *
 * <p><b>两个必须处理对的地方</b>：
 * <ol>
 *   <li><b>多个 XML 文档首尾相接</b>（每条语句一个独立 `&lt;?xml ...?&gt;`），不能当整体解析；</li>
 *   <li><b>NULL 是 `xsi:nil="true"` 的空元素</b>，要变成判题侧的 NULL 标记而不是空串。</li>
 * </ol>
 *
 * <p>不引 XML 库：输出形态由 MySQL 客户端固定生成（不是用户可控的任意 XML），用正则切
 * `<resultset>` 与 `<field>` 足够，也省掉一份依赖与 XXE 面。
 */
final class SqlResultCodec {

    /**
     * 判题侧的 NULL 标记，与 `frontend/.../sql-judge.ts` 和 `scripts/sql_judge.py` 的常量一致。
     * XML 里 NULL 是 `xsi:nil="true"`，到这里统一成这个串，判题侧就不必理解 XML。
     */
    static final String NULL_MARKER = "\u0000NULL";

    private static final Pattern RESULTSET_OPEN = Pattern.compile("<resultset\\b[^>]*>");
    private static final Pattern RESULTSET_CLOSE = Pattern.compile("</resultset>");
    private static final Pattern ROW_OPEN = Pattern.compile("<row\\b[^>]*>");
    private static final Pattern FIELD =
            Pattern.compile("<field\\b([^>]*?)(?:/>|>(.*?)</field>)", Pattern.DOTALL);
    private static final Pattern NAME_ATTR = Pattern.compile("name=\"([^\"]*)\"");
    private static final Pattern NIL_ATTR = Pattern.compile("xsi:nil=\"(?:true|1)\"");

    private SqlResultCodec() {
    }

    /** 解析结果：结果集 + 是否超限。超限时结果集为空，由调用方报 `OVER_LIMIT`。 */
    record Parsed(List<CodeRun.Table> tables, boolean overLimit) {
    }

    /**
     * @param stdout        容器返回的原始 stdout
     * @param maxRowsPerSet 单个结果集的行数上限
     */
    static Parsed parse(String stdout, int maxRowsPerSet) {
        if (stdout == null || stdout.isEmpty()) {
            return new Parsed(List.of(), false);
        }
        int limit = Math.max(1, maxRowsPerSet);
        List<CodeRun.Table> tables = new ArrayList<>();
        boolean overLimit = false;
        int statementIndex = 0;

        // 每条语句一个独立 XML 文档，首尾相接：按 <resultset> 的位置逐段处理
        Matcher openings = RESULTSET_OPEN.matcher(stdout);
        List<Integer> bodyStarts = new ArrayList<>();
        while (openings.find()) {
            bodyStarts.add(openings.end());
        }
        for (int index = 0; index < bodyStarts.size(); index++) {
            int from = bodyStarts.get(index);
            int end = stdout.length();
            if (index + 1 < bodyStarts.size()) {
                Matcher closes = RESULTSET_CLOSE.matcher(stdout);
                if (closes.find(from)) {
                    end = Math.min(end, closes.start());
                }
            } else {
                Matcher closes = RESULTSET_CLOSE.matcher(stdout);
                if (closes.find(from)) {
                    end = closes.start();
                }
            }
            ParsedSet parsedSet = parseResultSet(stdout.substring(from, end), limit);
            overLimit |= parsedSet.overLimit();
            tables.add(new CodeRun.Table(statementIndex++, parsedSet.columns(), parsedSet.rows()));
        }
        if (overLimit) {
            // 超限不是"部分成功"：丢掉已读到的一切，让上层报 OVER_LIMIT 而不是拿半截结果判对
            return new Parsed(List.of(), true);
        }
        return new Parsed(List.copyOf(tables), false);
    }

    private record ParsedSet(List<String> columns, List<List<String>> rows, boolean overLimit) {
    }

    private static ParsedSet parseResultSet(String body, int limit) {
        List<String> columns = List.of();
        List<List<String>> rows = new ArrayList<>();
        boolean overLimit = false;

        Matcher rows_ = ROW_OPEN.matcher(body);
        int cursor = 0;
        while (rows_.find(cursor)) {
            int rowStart = rows_.end();
            int rowEnd = body.indexOf("</row>", rowStart);
            if (rowEnd < 0) {
                rowEnd = body.length();
            }
            cursor = rowEnd;

            List<String> names = new ArrayList<>();
            List<String> values = new ArrayList<>();
            Matcher field = FIELD.matcher(body.substring(rowStart, rowEnd));
            while (field.find()) {
                String attributes = field.group(1) == null ? "" : field.group(1);
                String text = field.group(2) == null ? "" : field.group(2);
                Matcher nameAttr = NAME_ATTR.matcher(attributes);
                names.add(nameAttr.find() ? nameAttr.group(1) : "");
                values.add(NIL_ATTR.matcher(attributes).find() ? NULL_MARKER : unescape(text));
            }
            if (columns.isEmpty() && !names.isEmpty()) {
                columns = List.copyOf(names);
            }
            if (rows.size() < limit) {
                rows.add(List.copyOf(values));
            } else {
                overLimit = true;
            }
        }
        return new ParsedSet(columns, List.copyOf(rows), overLimit);
    }

    private static String unescape(String text) {
        if (text.indexOf('&') < 0) {
            return text;
        }
        return text.replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&apos;", "'")
                .replace("&amp;", "&");
    }
}
