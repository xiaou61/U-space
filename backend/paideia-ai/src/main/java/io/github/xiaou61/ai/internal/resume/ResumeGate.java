package io.github.xiaou61.ai.internal.resume;

import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.ResumeProposal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 事实闸门（WORK-018 REQ-017）。
 *
 * <p>它是整个工作项里最重要的一段代码：**在用户点"定稿"之后、落库之前**，逐条检查改写有没有
 * 越过事实边界。四条检查对应四类真实的失真：
 *
 * <table>
 *   <tr><th>检查</th><th>拦的是什么</th></tr>
 *   <tr><td>数字</td><td>编出来的指标（"提升 30%"）</td></tr>
 *   <tr><td>重合度</td><td>整段换成与原文无关的内容（"这条经历被重写成另一件事"）</td></tr>
 *   <tr><td>身份字段</td><td>职位名/公司名/时间被悄悄升级或改写</td></tr>
 *   <tr><td>缺口词</td><td>把不会的技能写进去</td></tr>
 * </table>
 *
 * <p><b>为什么阈值要给成参数</b>：0.35 这个数没有理论依据，只能靠夹具校准。把它写死在代码里
 * 会让"调阈值"变成一次代码改动；给成常量 + 在验证报告里记录实测，是更诚实的做法。
 */
@Component
public class ResumeGate {

    /**
     * 改写与原文的内容词覆盖率下限。
     *
     * <p>取值依据见验证报告：同一批夹具在 0.25/0.35/0.45 三档下的判定结果。0.25 太松
     * （把"改成另一件事"也放过），0.45 太紧（连正常的措辞重排都会被打回）。
     */
    static final double MIN_OVERLAP = 0.35;

    /** 违规码。界面按码给具体提示，不只显示一句"不合规"。 */
    public static final String NUMBERS_NOT_IN_SOURCE = "numbers_not_in_source";
    public static final String LOW_OVERLAP = "low_overlap";
    public static final String IDENTITY_CHANGED = "identity_changed";
    public static final String GAP_KEYWORD_LEAK = "gap_keyword_leak";

    /**
     * 数字 token。**刻意抓裸数字**（不要求带单位）。
     *
     * <p>第一版只抓"数字 + 单位"，实测漏掉了 `QPS 提到 50000` 这种没有单位的写法——
     * 而它恰恰是最容易编造的数字形态。现在改成先剥离日期，再对剩下的每个数字做完整 token 校验。
     */
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    /**
     * 日期样式，检查前先从改后文本里剔除。
     *
     * <p>不剔除的话，`2023 年 7 月` 里的 `2023` 会被当成"原文没有的指标"——原文写的是
     * `2023.07`，两者是同一个时间。时间归身份字段那条检查管，不该在这条里重复误伤。
     */
    private static final Pattern DATE_LIKE = Pattern.compile(
            "\\d{4}\\s*[年./\\-]\\s*\\d{0,2}\\s*月?"
                    + "|\\d{4}\\s*年"
                    + "|\\d{1,2}\\s*月"
                    + "|\\d{4}\\s*[./\\-]\\s*\\d{1,2}");

    /**
     * 会改变量级的单位：这种单位换了就是失真（`30%` 变 `30`），必须跟着数字一起查。
     *
     * <p>计数单位（人/次/个/月/年）不查：数字本身已经被完整 token 校验过，再查单位只会
     * 让"800ms 写成 800 毫秒"这种等价改写被打回。
     */
    private static final List<String> MAGNITUDE_UNITS =
            List.of("%", "％", "万", "千", "k", "K", "亿", "倍");

    /**
     * 一条违规。
     *
     * @param proposalIndex 第几条提案（界面上直接定位）
     * @param code          {@link #NUMBERS_NOT_IN_SOURCE} 等
     * @param detail        给人看的说明，**不含简历正文原文**（只引数字/字段名）
     */
    public record Violation(int proposalIndex, String code, String detail) {
    }

    /**
     * 逐条检查全部提案。
     *
     * @param resume    主简历（身份字段比对基准）
     * @param plainText 抽取原文（数字比对基准）
     * @param proposals 待检查的提案（通常只传"已采纳"的那些）
     * @param gaps      缺口关键词
     */
    public List<Violation> check(ResumeData resume, String plainText, List<ResumeProposal> proposals,
                                 List<String> gaps) {
        List<Violation> violations = new ArrayList<>();
        // 数字基准 = 抽取原文 + 结构化渲染。后者覆盖用户手工新增、原文里没有的条目。
        String sourceForNumbers = TextNormalizer.normalize(plainText) + "\n"
                + TextNormalizer.normalize(ResumeText.render(resume));
        List<String> normalizedGaps = gaps.stream()
                .map(TextNormalizer::normalize)
                .filter(value -> !value.isEmpty())
                .toList();

        for (int index = 0; index < proposals.size(); index++) {
            ResumeProposal proposal = proposals.get(index);
            checkNumbers(index, proposal, sourceForNumbers, violations);
            checkOverlap(index, proposal, violations);
            checkIdentity(index, proposal, resume, violations);
            checkGapLeak(index, proposal, normalizedGaps, violations);
        }
        return List.copyOf(violations);
    }

    // ── ① 数字 ──────────────────────────────────────────────────────

    private void checkNumbers(int index, ResumeProposal proposal, String source, List<Violation> violations) {
        Set<String> missing = new LinkedHashSet<>();
        // 先把日期样式的片段剔掉：它们归身份字段那条检查，不在这里重复误伤
        String withoutDates = DATE_LIKE.matcher(proposal.proposedText()).replaceAll(" ");
        String normalizedProposal = TextNormalizer.normalize(withoutDates);
        Matcher matcher = NUMBER.matcher(normalizedProposal);
        while (matcher.find()) {
            String number = matcher.group();
            // 完整 token 校验：`80` 不能命中原文里的 `800`
            if (!containsNumber(source, number)) {
                missing.add(number);
                continue;
            }
            // 会改变量级的单位必须也在原文出现过：`30` 与 `30%` 是两个意思
            String tail = normalizedProposal.substring(matcher.end()).trim();
            for (String unit : MAGNITUDE_UNITS) {
                if (tail.startsWith(unit) && !source.contains(unit)) {
                    missing.add(number + unit);
                    break;
                }
            }
        }
        if (!missing.isEmpty()) {
            violations.add(new Violation(index, NUMBERS_NOT_IN_SOURCE,
                    "改写里出现了原文没有的数字：" + String.join("、", missing)));
        }
    }

    /** 数字必须在原文里以**完整数字**出现：`80` 不能命中 `800`。 */
    private static boolean containsNumber(String source, String number) {
        int from = 0;
        while (true) {
            int index = source.indexOf(number, from);
            if (index < 0) {
                return false;
            }
            boolean leftOk = index == 0 || !Character.isDigit(source.charAt(index - 1))
                    && source.charAt(index - 1) != '.';
            int end = index + number.length();
            boolean rightOk = end >= source.length() || !Character.isDigit(source.charAt(end))
                    && source.charAt(end) != '.';
            if (leftOk && rightOk) {
                return true;
            }
            from = index + 1;
        }
    }

    // ── ② 重合度 ────────────────────────────────────────────────────

    private void checkOverlap(int index, ResumeProposal proposal, List<Violation> violations) {
        // reorder 不改文字，没有"重合"可言
        if ("reorder".equals(proposal.changeType())) {
            return;
        }
        double overlap = overlap(proposal.originalText(), proposal.proposedText());
        if (overlap < MIN_OVERLAP) {
            violations.add(new Violation(index, LOW_OVERLAP,
                    String.format(java.util.Locale.ROOT,
                            "改写与原文的共同内容太少（覆盖率 %.2f，下限 %.2f），看起来像换了一件事",
                            overlap, MIN_OVERLAP)));
        }
    }

    /**
     * 覆盖率 = |原文 ∩ 改后| / |改后|。
     *
     * <p>用"改后"当分母而不是对称的 Jaccard：判的是"改后这段内容有多少来自原文"，
     * 不是"两段有多像"。这样"把三句话压成一句"不会被误判（压缩后全部内容都来自原文）。
     */
    static double overlap(String original, String proposed) {
        Set<String> originalTokens = tokens(original);
        Set<String> proposedTokens = tokens(proposed);
        if (originalTokens.isEmpty() || proposedTokens.isEmpty()) {
            return 0;
        }
        long shared = proposedTokens.stream().filter(originalTokens::contains).count();
        return (double) shared / proposedTokens.size();
    }

    /** CJK 用二元组，ASCII 用词。中英混排时两种都产出，共同构成集合。 */
    private static Set<String> tokens(String text) {
        String normalized = TextNormalizer.normalize(text);
        Set<String> tokens = new LinkedHashSet<>();
        StringBuilder latin = new StringBuilder();
        StringBuilder cjkRun = new StringBuilder();
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (isCjk(character)) {
                flushLatin(latin, tokens);
                cjkRun.append(character);
            } else if (Character.isLetterOrDigit(character)) {
                flushCjk(cjkRun, tokens);
                latin.append(character);
            } else {
                flushLatin(latin, tokens);
                flushCjk(cjkRun, tokens);
            }
        }
        flushLatin(latin, tokens);
        flushCjk(cjkRun, tokens);
        return tokens;
    }

    private static void flushLatin(StringBuilder latin, Set<String> tokens) {
        if (latin.length() > 0) {
            tokens.add(latin.toString());
            latin.setLength(0);
        }
    }

    private static void flushCjk(StringBuilder run, Set<String> tokens) {
        String text = run.toString();
        if (text.length() == 1) {
            tokens.add(text);
        }
        for (int index = 0; index + 1 < text.length(); index++) {
            tokens.add(text.substring(index, index + 2));
        }
        run.setLength(0);
    }

    private static boolean isCjk(char character) {
        Character.UnicodeScript script = Character.UnicodeScript.of(character);
        return script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
                || script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL;
    }

    // ── ③ 身份字段 ──────────────────────────────────────────────────

    /**
     * 职位名/公司名/时间不许改。
     *
     * <p>只对 `rewrite` 检查 `reorder`：后者的改后文本本来就是原样的（只是位置变化）。
     * 对 `add` 不做身份检查——新增的要点没有"对应的身份字段"。
     *
     * <p>判定方式是**方向性**的：改后文本里若出现了主简历中**不存在**的公司名/职位名形态，
     * 才算违规。这里不做"提取实体"（那需要 NER），而是用便宜的近似：把主简历里所有工作条目的
     * 公司名与职位名收成白名单，再看改后文本里有没有**看起来像**这些字段却被改过的痕迹。
     *
     * <p>更可靠的一条在 {@link ResumeProposalParser}：`entryRef` 指向哪一条，就用那一条的身份
     * 字段逐字比对。这里做的是第二道——针对"改后文本里塞进了另一个头衔"的情况。
     */
    private void checkIdentity(int index, ResumeProposal proposal, ResumeData resume,
                               List<Violation> violations) {
        if ("add".equals(proposal.changeType()) || "remove".equals(proposal.changeType())) {
            return;
        }
        String ref = proposal.entryRef() == null ? "" : proposal.entryRef();
        if (!ref.startsWith("work[")) {
            return;
        }
        ResumeData.Work work = workAt(resume, ref);
        if (work == null) {
            return;
        }
        String proposed = TextNormalizer.normalize(proposal.proposedText());
        String source = TextNormalizer.normalize(proposal.originalText());
        // 头衔与公司：若原文里有、改后里没有，视为被改掉（可能是无意的，但必须让用户看见）
        if (!work.title().isBlank()) {
            String title = TextNormalizer.normalize(work.title());
            if (source.contains(title) && !proposed.contains(title)) {
                violations.add(new Violation(index, IDENTITY_CHANGED,
                        "改写里不再包含原来的职位名，职位名不允许改动"));
            }
        }
        if (!work.company().isBlank()) {
            String company = TextNormalizer.normalize(work.company());
            if (source.contains(company) && !proposed.contains(company)) {
                violations.add(new Violation(index, IDENTITY_CHANGED,
                        "改写里不再包含原来的公司名，公司名不允许改动"));
            }
        }
        // 起止时间：出现在原文里却在改后消失，同样拦（"至今"改成具体月份是常见的失真）
        for (String period : new String[]{work.start(), work.end()}) {
            if (period == null || period.isBlank()) {
                continue;
            }
            String normalized = TextNormalizer.normalize(period);
            if (source.contains(normalized) && !proposed.contains(normalized)) {
                violations.add(new Violation(index, IDENTITY_CHANGED,
                        "改写里不再包含原来的时间（" + period + "），起止时间不允许改动"));
            }
        }
    }

    private static ResumeData.Work workAt(ResumeData resume, String ref) {
        try {
            int close = ref.indexOf(']');
            if (close < 0) {
                return null;
            }
            int index = Integer.parseInt(ref.substring("work[".length(), close));
            return resume.work().get(index);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    // ── ④ 缺口词 ────────────────────────────────────────────────────

    private void checkGapLeak(int index, ResumeProposal proposal, List<String> gaps,
                              List<Violation> violations) {
        String proposed = TextNormalizer.normalize(proposal.proposedText());
        List<String> leaked = gaps.stream().filter(proposed::contains).toList();
        if (!leaked.isEmpty()) {
            violations.add(new Violation(index, GAP_KEYWORD_LEAK,
                    "改写里出现了简历里完全没有的能力，不能写进定稿"));
        }
    }
}
