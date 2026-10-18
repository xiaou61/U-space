package io.github.xiaou61.ai.internal.resume;

import io.github.xiaou61.ai.ResumeAnalysis;
import io.github.xiaou61.ai.ResumeData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 关键词三桶分类（WORK-018 REQ-013）。
 *
 * <p><b>这一件事刻意不交给模型。</b> 模型自评"候选人会不会某技能"是幻觉的温床：它会把 JD 里
 * 出现的词顺手说成"他会"，而那种错误用户看不出来。字符串查找则完全可测、可复现、不花 token。
 *
 * <p>三桶的语义边界：
 * <ul>
 *   <li><b>现有</b>：命中技能区（skills 分组的组名或条目）。这是候选人自己声明过的能力。</li>
 *   <li><b>有证据</b>：命中简历其他位置（经历、项目、自我评价）。说明做过，但没写进技能区——
 *       这是"可以补进技能区"的正规素材。</li>
 *   <li><b>缺口</b>：全文没有。**这一类绝不能出现在任何改写里。**</li>
 * </ul>
 *
 * <p><b>等价表刻意只有 10 组</b>：多一组就多一个误判的面，而误判的方向是"把缺口说成我有"，
 * 比漏报危险得多（career-ops 的 `jd-skill-gap` 也把这条写成了纪律）。未命中一律归缺口——
 * 保守方向。
 */
@Component
public class ResumeKeywordClassifier {

    /** 桶名。落库与界面都直接用这三个值。 */
    public static final String BUCKET_EXISTING = "existing";
    public static final String BUCKET_EVIDENCED = "evidenced";
    public static final String BUCKET_GAP = "gap";

    /**
     * 等价写法。每个内层数组是一组"同一个东西的不同说法"，双向都算命中。
     *
     * <p>只放**确定等价**的：`k8s` 就是 Kubernetes，`js` 就是 JavaScript。不放任何"相关"关系
     * （`docker` 与 `kubernetes` 不互认，`aws` 与 `cloud` 不互认）——那会虚报能力。
     */
    private static final List<List<String>> EQUIVALENTS = List.of(
            List.of("k8s", "kubernetes"),
            List.of("js", "javascript"),
            List.of("ts", "typescript"),
            List.of("postgres", "postgresql"),
            List.of("ml", "machine learning"),
            List.of("cicd", "ci/cd"),
            List.of("iac", "infrastructure as code"),
            List.of("nlp", "natural language processing"),
            List.of("llm", "large language model", "large language models"),
            List.of("aws", "amazon web services"));

    /**
     * 一条分类结果。
     *
     * @param term   JD 要求本身
     * @param quote  JD 里的原句
     * @param bucket {@link #BUCKET_EXISTING} / {@link #BUCKET_EVIDENCED} / {@link #BUCKET_GAP}
     * @param hit    命中的地方（技能区某组 / 经历第几条）；缺口时为 null
     */
    public record Classified(String term, String quote, String bucket, String hit) {
    }

    /**
     * @param requirements 模型从 JD 抽出的显式要求
     * @param data         主简历
     * @param plainText    抽取原文（用户手工新增的条目不在原文里，所以还要看结构化渲染）
     */
    public List<Classified> classify(ResumeAnalysis analysis, ResumeData data, String plainText) {
        String skillsText = TextNormalizer.normalize(ResumeText.renderSkills(data));
        String allStructured = TextNormalizer.normalize(ResumeText.render(data));
        String allText = TextNormalizer.normalize(plainText);

        List<Classified> results = new ArrayList<>();
        for (ResumeAnalysis.Requirement requirement : analysis.requirements()) {
            List<String> forms = forms(requirement.term());
            String hitInSkills = firstHit(forms, skillsText);
            if (hitInSkills != null) {
                results.add(new Classified(requirement.term(), requirement.quote(),
                        BUCKET_EXISTING, hitInSkills));
                continue;
            }
            String hitElsewhere = firstHit(forms, allStructured + "\n" + allText);
            results.add(new Classified(requirement.term(), requirement.quote(),
                    hitElsewhere == null ? BUCKET_GAP : BUCKET_EVIDENCED, hitElsewhere));
        }
        return List.copyOf(results);
    }

    /** 缺口清单：只取 term，给提案那一步的"不许出现"清单用。 */
    public List<String> gaps(List<Classified> classified) {
        return classified.stream()
                .filter(item -> BUCKET_GAP.equals(item.bucket()))
                .map(Classified::term)
                .toList();
    }

    // ── 匹配 ────────────────────────────────────────────────────────

    /** 一条要求的全部可接受写法（自身 + 等价组里的其他写法），都已归一化。 */
    private static List<String> forms(String term) {
        String normalized = TextNormalizer.normalize(term);
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<String> forms = new ArrayList<>();
        forms.add(normalized);
        for (List<String> group : EQUIVALENTS) {
            boolean inGroup = group.stream().anyMatch(item -> item.equals(normalized));
            if (inGroup) {
                for (String item : group) {
                    if (!forms.contains(item)) {
                        forms.add(item);
                    }
                }
            }
        }
        return forms;
    }

    /** 返回第一个命中的写法，没有则 null。 */
    private static String firstHit(List<String> forms, String haystack) {
        if (haystack == null || haystack.isEmpty()) {
            return null;
        }
        for (String form : forms) {
            if (contains(haystack, form)) {
                return form;
            }
        }
        return null;
    }

    /**
     * 判断 {@code form} 是否出现在 {@code haystack} 里。
     *
     * <p>两条路径，因为边界规则不同：
     * <ul>
     *   <li><b>含 CJK</b>：子串匹配。中文没有词边界，`高并发` 就该在 `有高并发经验` 里命中。</li>
     *   <li><b>纯 ASCII</b>：词边界匹配。否则 `java` 会在 `javascript` 里命中、`c` 会在
     *       任何单词里命中——那是最典型的误报。</li>
     * </ul>
     */
    private static boolean contains(String haystack, String form) {
        if (form.isEmpty()) {
            return false;
        }
        if (hasCjk(form)) {
            return haystack.contains(form);
        }
        int from = 0;
        while (true) {
            int index = haystack.indexOf(form, from);
            if (index < 0) {
                return false;
            }
            boolean leftOk = index == 0 || !isWordChar(haystack.charAt(index - 1));
            int end = index + form.length();
            boolean rightOk = end >= haystack.length() || !isWordChar(haystack.charAt(end));
            if (leftOk && rightOk) {
                return true;
            }
            from = index + 1;
        }
    }

    private static boolean isWordChar(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '+'
                || character == '#' || character == '.';
    }

    private static boolean hasCjk(String value) {
        for (int index = 0; index < value.length(); index++) {
            Character.UnicodeScript script = Character.UnicodeScript.of(value.charAt(index));
            if (script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL) {
                return true;
            }
        }
        return false;
    }

    /** 给界面用：把分类结果按桶分组（保持 JD 原有顺序）。 */
    public Map<String, List<Classified>> group(List<Classified> classified) {
        Map<String, List<Classified>> grouped = new LinkedHashMap<>();
        grouped.put(BUCKET_EXISTING, new ArrayList<>());
        grouped.put(BUCKET_EVIDENCED, new ArrayList<>());
        grouped.put(BUCKET_GAP, new ArrayList<>());
        for (Classified item : classified) {
            grouped.get(item.bucket()).add(item);
        }
        return grouped;
    }

    /** 供调试日志：只输出计数，不输出内容（简历内容不得进日志）。 */
    public String describeCounts(List<Classified> classified) {
        long existing = classified.stream().filter(it -> BUCKET_EXISTING.equals(it.bucket())).count();
        long evidenced = classified.stream().filter(it -> BUCKET_EVIDENCED.equals(it.bucket())).count();
        long gap = classified.stream().filter(it -> BUCKET_GAP.equals(it.bucket())).count();
        return String.format(Locale.ROOT, "existing=%d evidenced=%d gap=%d", existing, evidenced, gap);
    }
}
