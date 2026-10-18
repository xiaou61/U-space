package io.github.xiaou61.ai.internal.resume;

import io.github.xiaou61.ai.ResumeData;
import java.util.List;

/**
 * 把结构化简历渲染成**给模型看的纯文本**（WORK-018）。
 *
 * <p>为什么不直接把 JSON 丢进提示词：模型对"一段带分节标题的简历文本"的解析质量明显比
 * 对嵌套 JSON 稳定，而且同一份渲染结果可以直接给用户看（"模型看到的就是这个"）。
 *
 * <p>它同时承担"事实闸门的比对基准"的一部分：闸门找不到主简历原文时（用户手工新增的条目），
 * 就用这份渲染结果当基准。渲染必须**只搬运不加工**——少一个数字都会让闸门误判。
 */
public final class ResumeText {

    private ResumeText() {
    }

    /** 渲染成 marker 分节的中文文本。空分区不输出标题，避免模型对着空标题编内容。 */
    public static String render(ResumeData data) {
        StringBuilder builder = new StringBuilder();
        if (data == null) {
            return "";
        }

        ResumeData.Basics basics = data.basics();
        if (basics != null) {
            section(builder, "基本信息");
            line(builder, "姓名", basics.name());
            line(builder, "电话", basics.phone());
            line(builder, "邮箱", basics.email());
            line(builder, "城市", basics.city());
            if (basics.links() != null && !basics.links().isEmpty()) {
                line(builder, "链接", String.join(" ", basics.links()));
            }
        }

        ResumeData.Intent intent = data.intent();
        if (intent != null && (notBlank(intent.role()) || notBlank(intent.city()) || notBlank(intent.availability()))) {
            section(builder, "求职意向");
            line(builder, "目标岗位", intent.role());
            line(builder, "目标城市", intent.city());
            line(builder, "到岗时间", intent.availability());
        }

        if (!data.education().isEmpty()) {
            section(builder, "教育经历");
            for (ResumeData.Education item : data.education()) {
                builder.append("- ").append(nz(item.school()));
                if (notBlank(item.degree())) {
                    builder.append(" / ").append(item.degree());
                }
                if (notBlank(item.major())) {
                    builder.append(" / ").append(item.major());
                }
                builder.append(" (").append(nz(item.start())).append(" - ").append(nz(item.end())).append(")\n");
                bullets(builder, item.highlights());
            }
        }

        if (!data.work().isEmpty()) {
            section(builder, "工作与实习经历");
            for (ResumeData.Work item : data.work()) {
                builder.append("- ").append(nz(item.company())).append(" | ").append(nz(item.title()))
                        .append(" | ").append(nz(item.start())).append(" - ").append(nz(item.end())).append("\n");
                if (notBlank(item.summary())) {
                    builder.append("  概要：").append(item.summary()).append("\n");
                }
                bullets(builder, item.bullets());
            }
        }

        if (!data.projects().isEmpty()) {
            section(builder, "项目经历");
            for (ResumeData.Project item : data.projects()) {
                builder.append("- ").append(nz(item.name()));
                if (notBlank(item.role())) {
                    builder.append(" | ").append(item.role());
                }
                if (notBlank(item.period())) {
                    builder.append(" | ").append(item.period());
                }
                builder.append("\n");
                bullets(builder, item.bullets());
            }
        }

        if (!data.skills().isEmpty()) {
            section(builder, "技能");
            for (ResumeData.SkillGroup group : data.skills()) {
                builder.append("- ").append(nz(group.group())).append("：")
                        .append(String.join("、", group.items())).append("\n");
            }
        }

        if (!data.awards().isEmpty()) {
            section(builder, "证书与奖项");
            for (ResumeData.Award award : data.awards()) {
                builder.append("- ").append(nz(award.name()));
                if (notBlank(award.issuer())) {
                    builder.append(" | ").append(award.issuer());
                }
                if (notBlank(award.date())) {
                    builder.append(" | ").append(award.date());
                }
                builder.append("\n");
            }
        }

        if (notBlank(data.selfEvaluation())) {
            section(builder, "自我评价");
            builder.append(data.selfEvaluation()).append("\n");
        }

        return builder.toString().trim();
    }

    /** 只渲染技能区，给三桶分类判断"这块算不算技能区写过"。 */
    public static String renderSkills(ResumeData data) {
        StringBuilder builder = new StringBuilder();
        if (data != null) {
            for (ResumeData.SkillGroup group : data.skills()) {
                builder.append(nz(group.group())).append(' ');
                builder.append(String.join(" ", group.items())).append('\n');
            }
        }
        return builder.toString();
    }

    /** 分析结果渲染（给提案那一步看）。 */
    public static String renderAnalysis(List<ResumeAnalysisView> requirements, String summary) {
        StringBuilder builder = new StringBuilder();
        if (notBlank(summary)) {
            builder.append("岗位要点：").append(summary).append("\n\n");
        }
        for (ResumeAnalysisView item : requirements) {
            builder.append("- ").append(item.term());
            if (notBlank(item.quote())) {
                builder.append("  ← 「").append(item.quote()).append("」");
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private static void section(StringBuilder builder, String title) {
        if (builder.length() > 0) {
            builder.append("\n");
        }
        builder.append("【").append(title).append("】\n");
    }

    private static void line(StringBuilder builder, String label, String value) {
        if (notBlank(value)) {
            builder.append(label).append("：").append(value).append("\n");
        }
    }

    private static void bullets(StringBuilder builder, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (notBlank(value)) {
                builder.append("  • ").append(value).append("\n");
            }
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
