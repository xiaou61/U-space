package io.github.xiaou61.ai.internal.resume;

import io.github.xiaou61.ai.ResumeData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 简历体检（WORK-018 REQ-010）。
 *
 * <p><b>纯函数、不调用模型、不出分数。</b> 每一项只回答"通过 / 待改"并给出位置——
 * 理由见需求 REQ-011：0–100 分无法校准，而一个假精确的分数会诱导用户把精力放在刷分上，
 * 而不是去修那几条具体的毛病。
 *
 * <p>九项检查，每项都有一个明确的"什么样的简历该被判待改"，不写"建议优化"这种放之四海皆准的话。
 */
@Component
public class ResumeCheckup {

    /** 一项检查结果。{@code location} 是给用户看的定位（如"工作经历第 1 条"）。 */
    public record Item(String code, String title, boolean ok, String detail, String location) {
    }

    private static final Pattern DATE = Pattern.compile(
            "^(\\d{4}([./-]\\d{1,2})?|\\d{4}\\s*年(\\s*\\d{1,2}\\s*月)?|至今|现在|present)$");

    public List<Item> check(ResumeData data) {
        List<Item> items = new ArrayList<>();
        items.add(contacts(data));
        items.add(intent(data));
        items.add(skills(data));
        items.add(periods(data));
        items.add(dateFormat(data));
        items.add(quantifiedBullets(data));
        items.add(length(data));
        items.add(duplicates(data));
        items.add(emptySections(data));
        return List.copyOf(items);
    }

    private Item contacts(ResumeData data) {
        ResumeData.Basics basics = data.basics();
        List<String> missing = new ArrayList<>();
        if (blank(basics.name())) {
            missing.add("姓名");
        }
        if (blank(basics.phone()) && blank(basics.email())) {
            missing.add("电话与邮箱（至少填一个）");
        }
        return missing.isEmpty()
                ? pass("contacts", "联系方式", "姓名与联系方式都在")
                : fail("contacts", "联系方式", "缺少：" + String.join("、", missing), "基本信息");
    }

    private Item intent(ResumeData data) {
        return blank(data.intent().role())
                ? fail("intent", "求职意向", "没有写目标岗位；HR 需要一眼知道你想做什么", "求职意向")
                : pass("intent", "求职意向", "目标岗位：" + data.intent().role());
    }

    private Item skills(ResumeData data) {
        boolean has = data.skills().stream().anyMatch(group -> !group.items().isEmpty());
        return has ? pass("skills", "技能区", "有技能分组")
                : fail("skills", "技能区", "没有技能区；关键词检索的第一落点就在这里", "技能");
    }

    private Item periods(ResumeData data) {
        List<String> incomplete = new ArrayList<>();
        for (int index = 0; index < data.work().size(); index++) {
            ResumeData.Work work = data.work().get(index);
            if (blank(work.start()) || blank(work.end())) {
                incomplete.add("工作经历第 " + (index + 1) + " 条");
            }
        }
        for (int index = 0; index < data.education().size(); index++) {
            ResumeData.Education education = data.education().get(index);
            if (blank(education.start()) || blank(education.end())) {
                incomplete.add("教育经历第 " + (index + 1) + " 条");
            }
        }
        return incomplete.isEmpty()
                ? pass("periods", "起止时间完整", "每段经历都有起止时间")
                : fail("periods", "起止时间完整", "缺起止时间：" + String.join("、", incomplete),
                        String.join("、", incomplete));
    }

    private Item dateFormat(ResumeData data) {
        Set<String> styles = new HashSet<>();
        List<String> odd = new ArrayList<>();
        for (ResumeData.Work work : data.work()) {
            for (String period : new String[]{work.start(), work.end()}) {
                if (blank(period)) {
                    continue;
                }
                String trimmed = period.trim();
                if (!DATE.matcher(trimmed).matches()) {
                    odd.add(trimmed);
                } else {
                    styles.add(style(trimmed));
                }
            }
        }
        if (!odd.isEmpty()) {
            return fail("dateFormat", "时间格式一致", "这些时间写法不规范：" + String.join("、", odd),
                    "工作经历");
        }
        return styles.size() > 1
                ? fail("dateFormat", "时间格式一致",
                        "同时用了多种写法（如 2023.07 与 2023 年 7 月）；统一一种更易被解析", "工作经历")
                : pass("dateFormat", "时间格式一致", "时间写法统一");
    }

    private Item quantifiedBullets(ResumeData data) {
        int total = 0;
        int quantified = 0;
        for (ResumeData.Work work : data.work()) {
            for (String bullet : work.bullets()) {
                total++;
                if (bullet.chars().anyMatch(Character::isDigit)) {
                    quantified++;
                }
            }
        }
        for (ResumeData.Project project : data.projects()) {
            for (String bullet : project.bullets()) {
                total++;
                if (bullet.chars().anyMatch(Character::isDigit)) {
                    quantified++;
                }
            }
        }
        if (total == 0) {
            return fail("quantified", "要点带数字", "还没有任何经历要点", "工作经历");
        }
        // 阈值取"至少三分之一"：全都要有数字不现实（很多事就是定性的），
        // 一条都没有则说明整份简历没有任何可验证的结果。
        double ratio = (double) quantified / total;
        return ratio >= 0.34
                ? pass("quantified", "要点带数字", quantified + " / " + total + " 条要点含数字")
                : fail("quantified", "要点带数字",
                        "只有 " + quantified + " / " + total + " 条要点含数字；结果最好可验证",
                        "工作经历 / 项目");
    }

    private Item length(ResumeData data) {
        int chars = ResumeText.render(data).length();
        // 一页 A4 中文约 1200–1600 字。超过 4000 字基本是两页以上，需要用户自己判断。
        return chars <= 4000
                ? pass("length", "篇幅", "约 " + chars + " 字")
                : fail("length", "篇幅", "已约 " + chars + " 字，打印后很可能超过两页", "整份简历");
    }

    private Item duplicates(ResumeData data) {
        Map<String, Integer> seen = new HashMap<>();
        for (ResumeData.Work work : data.work()) {
            for (String bullet : work.bullets()) {
                seen.merge(TextNormalizer.normalize(bullet), 1, Integer::sum);
            }
        }
        List<String> duplicated = seen.entrySet().stream()
                .filter(entry -> entry.getValue() > 1 && !entry.getKey().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
        return duplicated.isEmpty()
                ? pass("duplicates", "要点重复", "没有完全重复的要点")
                : fail("duplicates", "要点重复", "有 " + duplicated.size() + " 条要点出现了两次以上",
                        "工作经历");
    }

    private Item emptySections(ResumeData data) {
        List<String> empty = new ArrayList<>();
        if (data.education().isEmpty()) {
            empty.add("教育经历");
        }
        if (data.work().isEmpty()) {
            empty.add("工作经历");
        }
        if (data.skills().isEmpty()) {
            empty.add("技能");
        }
        return empty.isEmpty()
                ? pass("sections", "主要分区", "教育、工作、技能都有内容")
                : fail("sections", "主要分区", "这些分区是空的：" + String.join("、", empty),
                        String.join("、", empty));
    }

    private static String style(String period) {
        if (period.matches("\\d{4}[./-]\\d{1,2}")) {
            return "numeric";
        }
        if (period.matches("\\d{4}\\s*年.*")) {
            return "cjk";
        }
        if (period.matches("\\d{4}")) {
            return "year";
        }
        return "text";
    }

    private static Item pass(String code, String title, String detail) {
        return new Item(code, title, true, detail, null);
    }

    private static Item fail(String code, String title, String detail, String location) {
        return new Item(code, title, false, detail, location);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
