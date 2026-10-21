package io.github.xiaou61.ai.internal.camp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 题目结构校验（WORK-029）。
 *
 * <p>模型返回的每一道题都必须**全部**过这九条，任一不过即丢弃——不改写、不补全。
 * 「不修改题目内容去修好它」是刻意的：我们不知道正确答案是什么，改了只会让一道坏题
 * 看起来像好题。
 *
 * <p><b>解析锚定</b>（{@link Reason#EXPLANATION_NOT_ANCHORED}）是这套校验里最有价值的一条：
 * 模型给出的正确选项和它自己写的解析对不上，说明它自己也乱了，这道题不能信。
 *
 * <p>纯函数：不碰库、不调模型。
 */
public final class ItemValidator {

    static final int STEM_MIN = 8;
    static final int STEM_MAX = 300;
    static final int OPTION_COUNT = 4;
    static final int OPTION_MIN = 2;
    static final int OPTION_MAX = 120;
    static final int EXPLANATION_MIN = 10;
    static final int EXPLANATION_MAX = 600;
    static final int TOPIC_MAX = 60;
    /** 解析必须锚定的正确选项字数。太短会碰巧命中，太长则模型改写后必然不中。 */
    static final int ANCHOR_CHARS = 6;

    /** 丢弃原因。枚举而不是字符串：它是"哪条校验最常命中"的唯一统计依据。 */
    public enum Reason {
        STEM_LENGTH,
        OPTION_COUNT,
        OPTION_LENGTH,
        OPTION_DUPLICATED,
        ANSWER_INDEX,
        EXPLANATION_LENGTH,
        EXPLANATION_NOT_ANCHORED,
        DIFFICULTY,
        TOPIC_LENGTH,
        DUPLICATE_STEM
    }

    /** 模型原始输出的一题。字段名与提示词里的 JSON 键一一对应。 */
    public record RawItem(String topic, String conceptName, String difficulty, String stem,
                          List<String> options, int answerIndex, String explanation) {
    }

    /**
     * 通过校验的一题。
     *
     * <p>{@code conceptId} 是**解析后的 id**（概念名对不上时为 null，题保留）；
     * {@code fingerprint} 由服务端按题干算，客户端的题干进不了这一列。
     */
    public record Accepted(int index, RawItem item, Long conceptId, String fingerprint) {
    }

    public record Violation(int index, Reason reason, String detail) {
    }

    public record Validated(List<Accepted> accepted, List<Violation> dropped) {
    }

    private ItemValidator() {
    }

    /**
     * @param knownConceptIds 概念规范名（小写）→ id。空表时所有题都会落成"没有概念"
     */
    public static Validated validate(List<RawItem> raw, Map<String, Long> knownConceptIds) {
        List<Accepted> accepted = new ArrayList<>();
        List<Violation> dropped = new ArrayList<>();
        if (raw == null) {
            return new Validated(List.of(), List.of());
        }
        Set<String> fingerprints = new HashSet<>();
        Map<String, Long> concepts = knownConceptIds == null ? Map.of() : knownConceptIds;

        for (int index = 0; index < raw.size(); index++) {
            RawItem item = raw.get(index);
            Reason failure = firstFailure(item);
            if (failure == null) {
                String fingerprint = CampFingerprint.of(item.stem());
                if (!fingerprints.add(fingerprint)) {
                    failure = Reason.DUPLICATE_STEM;
                } else {
                    accepted.add(new Accepted(index, item, resolveConcept(item.conceptName(), concepts),
                            fingerprint));
                    continue;
                }
            }
            dropped.add(new Violation(index, failure, detailOf(item, failure)));
        }
        return new Validated(List.copyOf(accepted), List.copyOf(dropped));
    }

    /**
     * 哪些题与已接受集合指纹冲突。复测用它核对"诊断题干确实被避开了"。
     */
    static Set<String> fingerprintsOf(List<Accepted> items) {
        Set<String> values = new HashSet<>();
        for (Accepted item : items) {
            values.add(item.fingerprint());
        }
        return values;
    }

    private static Reason firstFailure(RawItem item) {
        if (item == null) {
            return Reason.STEM_LENGTH;
        }
        String stem = trim(item.stem());
        if (stem.length() < STEM_MIN || stem.length() > STEM_MAX) {
            return Reason.STEM_LENGTH;
        }
        List<String> options = item.options();
        if (options == null || options.size() != OPTION_COUNT) {
            return Reason.OPTION_COUNT;
        }
        Set<String> seen = new HashSet<>();
        for (String option : options) {
            String value = trim(option);
            if (value.length() < OPTION_MIN || value.length() > OPTION_MAX) {
                return Reason.OPTION_LENGTH;
            }
            if (!seen.add(value)) {
                return Reason.OPTION_DUPLICATED;
            }
        }
        if (item.answerIndex() < 0 || item.answerIndex() >= OPTION_COUNT) {
            return Reason.ANSWER_INDEX;
        }
        String explanation = trim(item.explanation());
        if (explanation.length() < EXPLANATION_MIN || explanation.length() > EXPLANATION_MAX) {
            return Reason.EXPLANATION_LENGTH;
        }
        if (!anchored(explanation, options.get(item.answerIndex()))) {
            return Reason.EXPLANATION_NOT_ANCHORED;
        }
        if (!isDifficulty(item.difficulty())) {
            return Reason.DIFFICULTY;
        }
        String topic = trim(item.topic());
        if (topic.isEmpty() || topic.length() > TOPIC_MAX) {
            return Reason.TOPIC_LENGTH;
        }
        return null;
    }

    /** 解析里必须出现正确选项的前若干字（都去空白后比较，否则换行会让它永远对不上）。 */
    private static boolean anchored(String explanation, String correctOption) {
        String compactOption = stripSpaces(correctOption);
        String compactExplanation = stripSpaces(explanation);
        if (compactOption.isEmpty()) {
            return false;
        }
        String anchor = compactOption.substring(0, Math.min(ANCHOR_CHARS, compactOption.length()));
        return compactExplanation.contains(anchor);
    }

    private static Long resolveConcept(String conceptName, Map<String, Long> known) {
        if (conceptName == null || conceptName.isBlank()) {
            return null;
        }
        return known.get(trim(conceptName).toLowerCase(Locale.ROOT));
    }

    /**
     * 难度必须**原样**等于三个字面量之一。
     *
     * <p>刻意不 trim：{@code "hard "} 这种带空格的取值说明模型没按契约输出，与"解析对不上"
     * 同类——此时该丢题，而不是我们替它改正后当成好题。
     */
    private static boolean isDifficulty(String value) {
        return "easy".equals(value) || "medium".equals(value) || "hard".equals(value);
    }

    private static String detailOf(RawItem item, Reason reason) {
        if (item == null) {
            return "题目为空";
        }
        return switch (reason) {
            case STEM_LENGTH -> "题干长度 " + trim(item.stem()).length();
            case OPTION_COUNT -> "选项数 " + (item.options() == null ? 0 : item.options().size());
            case OPTION_LENGTH -> "存在过短或过长的选项";
            case OPTION_DUPLICATED -> "存在完全相同的选项";
            case ANSWER_INDEX -> "答案下标 " + item.answerIndex();
            case EXPLANATION_LENGTH -> "解析长度 " + trim(item.explanation()).length();
            case EXPLANATION_NOT_ANCHORED -> "解析与正确选项对不上";
            case DIFFICULTY -> "难度取值 " + item.difficulty();
            case TOPIC_LENGTH -> "主题长度 " + trim(item.topic()).length();
            case DUPLICATE_STEM -> "同卷重复题干";
        };
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String stripSpaces(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }
}
