package io.github.xiaou61.ai;

import java.util.List;

/**
 * 一份结构化简历（WORK-018）。
 *
 * <p><b>它是本项唯一真源</b>：编辑、提案、定稿、打印全部围绕它转，`plain_text` 只作为事实闸门的
 * 比对基准另存一列。字段刻意保持"人能一眼读懂、模型也能一次生成"的粒度，不做通用 JSON Resume
 * 那种多层嵌套——目标不是兼容别家格式，而是让"改一条要点"在界面上有确定的落点。
 *
 * <p><b>放在包根而不是 internal</b>：三个端口（{@code ResumeParser} / {@code ResumeAnalyzer} /
 * {@code ResumeProposer}）与 Web 视图都直接用它，而端口签名不得泄漏 internal 类型（D-09 同一纪律）。
 *
 * <p>解析阶段每个条目会多带 `quote`（原文引证）与 `section`；<b>它们不在这份结构里</b>：引证只在
 * 草稿核对页用一次，保存时丢弃。这样"用户可编辑的东西"与"模型给的证据"两条线不会混在一起
 * （用户改了文字，引证就失效了，留着只会误导）。
 */
public record ResumeData(
        Basics basics,
        Intent intent,
        List<Education> education,
        List<Work> work,
        List<Project> projects,
        List<SkillGroup> skills,
        List<Award> awards,
        String selfEvaluation) {

    /** 未识别到任何内容时的空值。编辑器与体检都靠它判断"有没有这块"。 */
    public static ResumeData empty() {
        return new ResumeData(
                new Basics("", "", "", "", List.of()),
                new Intent("", "", ""),
                List.of(), List.of(), List.of(), List.of(), List.of(), "");
    }

    public record Basics(String name, String phone, String email, String city, List<String> links) {
    }

    public record Intent(String role, String city, String availability) {
    }

    public record Education(String school, String degree, String major,
                            String start, String end, List<String> highlights) {
    }

    public record Work(String company, String title, String start, String end,
                       String summary, List<String> bullets) {
    }

    public record Project(String name, String role, String period, List<String> bullets) {
    }

    public record SkillGroup(String group, List<String> items) {
    }

    public record Award(String name, String issuer, String date) {
    }
}
