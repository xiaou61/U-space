package io.github.xiaou61.ai.internal.resume;

import io.github.xiaou61.ai.ResumeData;

/**
 * 提案里的位置引用 {@code entryRef} 的读与写（WORK-018）。
 *
 * <p>形态只有六种，刻意不通用化：
 * <pre>
 * work[i]                  → 该条经历的文本（公司/职位/时间/概要/要点拼在一起）
 * work[i].summary         → 该条经历的概要
 * work[i].bullets[j]      → 该条经历的第 j 条要点
 * projects[i]             → 该项目的文本
 * projects[i].bullets[j]  → 该项目第 j 条要点
 * skills[i].items[j]      → 第 i 个技能组的第 j 项
 * </pre>
 *
 * <p><b>为什么不引一个 JSON Pointer 库</b>：六种形态、每种的读取都只有一行，引库换来的只是
 * "能表达更多我们并不产生的形态"。而 {@code entryRef} 是模型给的字符串，<b>必须严格解析</b>——
 * 宽容地接受任意表达式等于让模型有机会指向别的东西。
 */
public final class ResumeRefs {

    private ResumeRefs() {
    }

    /**
     * 读 {@code entryRef} 指向的文本；位置不存在返回 {@code null}（调用方据此丢弃提案）。
     */
    static String read(ResumeData data, String entryRef) {
        if (data == null || entryRef == null) {
            return null;
        }
        String ref = entryRef.trim();
        try {
            if (ref.startsWith("work[")) {
                return readWork(data, ref);
            }
            if (ref.startsWith("projects[")) {
                return readProject(data, ref);
            }
            if (ref.startsWith("skills[")) {
                return readSkill(data, ref);
            }
        } catch (RuntimeException exception) {
            // 越界/数字格式不对都算"位置不存在"：模型给出的引用不可信，不当成系统错误
            return null;
        }
        return null;
    }

    private static String readWork(ResumeData data, String ref) {
        int close = ref.indexOf(']');
        if (close < 0) {
            return null;
        }
        int index = Integer.parseInt(ref.substring("work[".length(), close));
        ResumeData.Work work = data.work().get(index);
        String suffix = ref.substring(close + 1);
        if (suffix.isEmpty()) {
            return concat(work.company(), work.title(), work.start(), work.end(), work.summary(),
                    String.join(" ", work.bullets()));
        }
        if (".summary".equals(suffix)) {
            return work.summary();
        }
        if (suffix.startsWith(".bullets[")) {
            int bulletClose = suffix.indexOf(']', ".bullets[".length());
            int bulletIndex = Integer.parseInt(suffix.substring(".bullets[".length(), bulletClose));
            return work.bullets().get(bulletIndex);
        }
        return null;
    }

    private static String readProject(ResumeData data, String ref) {
        int close = ref.indexOf(']');
        if (close < 0) {
            return null;
        }
        int index = Integer.parseInt(ref.substring("projects[".length(), close));
        ResumeData.Project project = data.projects().get(index);
        String suffix = ref.substring(close + 1);
        if (suffix.isEmpty()) {
            return concat(project.name(), project.role(), project.period(), String.join(" ", project.bullets()));
        }
        if (suffix.startsWith(".bullets[")) {
            int bulletClose = suffix.indexOf(']', ".bullets[".length());
            int bulletIndex = Integer.parseInt(suffix.substring(".bullets[".length(), bulletClose));
            return project.bullets().get(bulletIndex);
        }
        return null;
    }

    private static String readSkill(ResumeData data, String ref) {
        int close = ref.indexOf(']');
        if (close < 0) {
            return null;
        }
        int index = Integer.parseInt(ref.substring("skills[".length(), close));
        ResumeData.SkillGroup group = data.skills().get(index);
        String suffix = ref.substring(close + 1);
        if (suffix.isEmpty()) {
            return group.group() + "：" + String.join("、", group.items());
        }
        if (suffix.startsWith(".items[")) {
            int itemClose = suffix.indexOf(']', ".items[".length());
            int itemIndex = Integer.parseInt(suffix.substring(".items[".length(), itemClose));
            return group.items().get(itemIndex);
        }
        return null;
    }

    private static String concat(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(part);
            }
        }
        return builder.toString();
    }
}
