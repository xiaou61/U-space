package io.github.xiaou61.ai.internal.camp;

import io.github.xiaou61.ai.ResumeData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 简历 → 技能画像（WORK-029）。
 *
 * <p><b>为什么是词表而不是把正文发给模型</b>：简历正文含姓名、电话、公司、学校，长度也不可控。
 * 这里只把**命中词表后的白名单术语**交给模型，模型拿不到任何原文。代价是词表外的技术栈识别
 * 不到——这是刻意的：宁可漏，也不外发原文。
 *
 * <p><b>覆盖度</b>用来给排期加权：一个词只出现在技能列表里（别人说他会）与它同时出现在项目
 * 和经历里（他真做过），可信度不同。{@code coverage = min(1, 出现的 section 数 / 3)}。
 *
 * <p>纯函数：不碰库、不读时钟、不调模型。
 */
public final class SkillDigest {

    /** 三类证据来源。顺序固定，保证 {@link Digest#coverage} 稳定。 */
    static final String SKILLS = "skills";
    static final String PROJECTS = "projects";
    static final String WORK = "work";

    public record Term(String text, Set<String> sections) {
    }

    /** 画像。{@code terms} 保持字典序，于是 {@link #promptBlock()} 的输出可复现。 */
    public record Digest(Map<String, Term> terms) {

        public boolean isEmpty() {
            return terms.isEmpty();
        }

        /**
         * 覆盖度。未知 term 返回 0（调用方据此判断"这个词不在画像里"）。
         *
         * <p>大小写不敏感：画像里的键是词表的规范写法，调用方可能拿到用户原文里的写法。
         */
        public double coverage(String term) {
            if (term == null) {
                return 0;
            }
            Term found = terms.get(term);
            if (found == null) {
                for (Term candidate : terms.values()) {
                    if (candidate.text().equalsIgnoreCase(term)) {
                        found = candidate;
                        break;
                    }
                }
            }
            return found == null ? 0 : Math.min(1.0, found.sections().size() / 3.0);
        }

        /**
         * 给模型看的一小段。**只含技术词**——姓名、电话、邮箱、公司、学校一律不出现。
         *
         * <p>上限 60 个词：提示词要短，而真实简历命中一两百个词是可能的。
         */
        public String promptBlock() {
            if (terms.isEmpty()) {
                return "（这份简历里没有识别到可考核的技术词）";
            }
            List<String> texts = new ArrayList<>(terms.size());
            for (Term term : terms.values()) {
                if (texts.size() >= 60) {
                    break;
                }
                texts.add(term.text());
            }
            return "技能：" + String.join("、", texts);
        }
    }

    private SkillDigest() {
    }

    public static Digest digest(ResumeData data) {
        Map<String, Set<String>> collected = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (data == null) {
            return new Digest(Collections.unmodifiableMap(new LinkedHashMap<>()));
        }

        // 技能区：条目本身就是术语（用户自己写的技术栈名），不再走词表
        Set<String> skillTerms = new LinkedHashSet<>();
        if (data.skills() != null) {
            for (ResumeData.SkillGroup group : data.skills()) {
                if (group == null || group.items() == null) {
                    continue;
                }
                for (String item : group.items()) {
                    String normalized = normalize(item);
                    if (!normalized.isEmpty()) {
                        skillTerms.add(normalized);
                    }
                }
            }
        }
        for (String term : skillTerms) {
            collected.computeIfAbsent(term, key -> new LinkedHashSet<>()).add(SKILLS);
        }

        // 项目与经历：只在词表命中的术语才算，避免把公司名、职责描述带出去
        StringBuilder projectText = new StringBuilder();
        if (data.projects() != null) {
            for (ResumeData.Project project : data.projects()) {
                if (project == null) {
                    continue;
                }
                append(projectText, project.name());
                append(projectText, project.role());
                append(projectText, project.bullets());
            }
        }
        StringBuilder workText = new StringBuilder();
        if (data.work() != null) {
            for (ResumeData.Work work : data.work()) {
                if (work == null) {
                    continue;
                }
                append(workText, work.title());
                append(workText, work.summary());
                append(workText, work.bullets());
            }
        }
        collect(projectText.toString(), PROJECTS, collected);
        collect(workText.toString(), WORK, collected);

        Map<String, Term> terms = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : collected.entrySet()) {
            terms.put(entry.getKey(), new Term(entry.getKey(),
                    Collections.unmodifiableSet(entry.getValue())));
        }
        return new Digest(Collections.unmodifiableMap(terms));
    }

    private static void collect(String text, String section, Map<String, Set<String>> collected) {
        if (text.isBlank()) {
            return;
        }
        boolean[] consumed = new boolean[text.length()];
        String lower = text.toLowerCase(Locale.ROOT);
        // 按别名长度降序逐个别名扫：长词先占位，"Spring Boot" 才不会被 "Spring" 吃掉
        for (Alias alias : ALIASES) {
            int from = 0;
            while (true) {
                int at = lower.indexOf(alias.text(), from);
                if (at < 0) {
                    break;
                }
                int end = at + alias.text().length();
                from = at + 1;
                if (!hasBoundary(lower, at, end) || overlaps(consumed, at, end)) {
                    continue;
                }
                for (int index = at; index < end; index++) {
                    consumed[index] = true;
                }
                collected.computeIfAbsent(alias.name(), key -> new LinkedHashSet<>()).add(section);
            }
        }
    }

    /**
     * ASCII 词的边界：前后都不能是**字母**（数字允许，"Java8" 仍是 Java）。
     * 这一条让 {@code Java} 不会命中 {@code JavaScript}，{@code HTTP} 不会命中 {@code HTTPS}。
     * 中文词的首尾天然是汉字，边界检查对它恒为真。
     */
    private static boolean hasBoundary(String lower, int start, int end) {
        boolean before = start == 0 || !isAsciiLetter(lower.charAt(start - 1));
        boolean after = end >= lower.length() || !isAsciiLetter(lower.charAt(end));
        return before && after;
    }

    private static boolean isAsciiLetter(char value) {
        return (value >= 'a' && value <= 'z') || (value >= 'A' && value <= 'Z');
    }

    private static boolean overlaps(boolean[] consumed, int start, int end) {
        for (int index = start; index < end; index++) {
            if (consumed[index]) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static void append(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(value).append('\n');
        }
    }

    private static void append(StringBuilder builder, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            append(builder, value);
        }
    }

    private record Tech(String name, List<String> aliases) {
    }

    private record Alias(String text, String name) {
    }

    /** 词表。`Tech` 一项可以有多个别名，全部映射到同一个规范名。 */
    private static final List<Tech> TECH = tech();

    /**
     * 拍平后的别名表，**按长度降序**。
     *
     * <p>为什么不按 Tech 分组扫：一组里最长的那个别名决定组间顺序会出错——
     * {@code Spring} 组的 "spring framework"（16 字）会把整组排到 {@code Spring Boot}（11 字）前面，
     * 于是 "spring" 先占掉 "spring boot" 的首词，"Spring Boot" 永远命不中。
     */
    private static final List<Alias> ALIASES = aliases();

    private static List<Tech> tech() {
        return List.of(
                new Tech("Spring Boot", List.of("spring boot", "springboot")),
                new Tech("Spring Cloud", List.of("spring cloud")),
                new Tech("Spring MVC", List.of("spring mvc")),
                new Tech("Spring", List.of("spring", "spring framework")),
                new Tech("MyBatis", List.of("mybatis")),
                new Tech("Hibernate", List.of("hibernate")),
                new Tech("Java", List.of("java")),
                new Tech("JavaScript", List.of("javascript", "js")),
                new Tech("TypeScript", List.of("typescript")),
                new Tech("JVM", List.of("jvm")),
                new Tech("JUC", List.of("juc")),
                new Tech("GC", List.of("垃圾回收", "garbage collection")),
                new Tech("集合", List.of("集合")),
                new Tech("并发", List.of("并发", "多线程", "concurrent")),
                new Tech("线程池", List.of("线程池", "thread pool")),
                new Tech("MySQL", List.of("mysql")),
                new Tech("PostgreSQL", List.of("postgresql", "postgres")),
                new Tech("Oracle", List.of("oracle")),
                new Tech("索引", List.of("索引", "index")),
                new Tech("事务", List.of("事务", "transaction")),
                new Tech("SQL", List.of("sql")),
                new Tech("Redis", List.of("redis")),
                new Tech("MongoDB", List.of("mongodb")),
                new Tech("Elasticsearch", List.of("elasticsearch", "es 集群")),
                new Tech("消息队列", List.of("消息队列", "message queue")),
                new Tech("Kafka", List.of("kafka")),
                new Tech("RabbitMQ", List.of("rabbitmq")),
                new Tech("RocketMQ", List.of("rocketmq")),
                new Tech("HTTP", List.of("http")),
                new Tech("HTTPS", List.of("https")),
                new Tech("TCP", List.of("tcp")),
                new Tech("UDP", List.of("udp")),
                new Tech("操作系统", List.of("操作系统")),
                new Tech("Linux", List.of("linux")),
                new Tech("算法", List.of("算法", "algorithm")),
                new Tech("数据结构", List.of("数据结构", "data structure")),
                new Tech("设计模式", List.of("设计模式", "design pattern")),
                new Tech("分布式", List.of("分布式", "distributed")),
                new Tech("微服务", List.of("微服务", "microservice")),
                new Tech("Docker", List.of("docker")),
                new Tech("Kubernetes", List.of("kubernetes", "k8s")),
                new Tech("CI/CD", List.of("ci/cd", "持续集成")),
                new Tech("Python", List.of("python")),
                new Tech("Go", List.of("golang")),
                new Tech("C++", List.of("c++")),
                new Tech("C#", List.of("c#")),
                new Tech("Kotlin", List.of("kotlin")),
                new Tech("Scala", List.of("scala")),
                new Tech("Rust", List.of("rust")),
                new Tech("React", List.of("react")),
                new Tech("Vue", List.of("vue")),
                new Tech("Angular", List.of("angular")),
                new Tech("Node.js", List.of("node.js", "nodejs")),
                new Tech("Nginx", List.of("nginx")),
                new Tech("JUnit", List.of("junit")),
                new Tech("Maven", List.of("maven")),
                new Tech("Gradle", List.of("gradle")),
                new Tech("Git", List.of("git")));
    }

    private static List<Alias> aliases() {
        List<Alias> flat = new ArrayList<>();
        for (Tech tech : TECH) {
            for (String alias : tech.aliases()) {
                flat.add(new Alias(alias, tech.name()));
            }
        }
        flat.sort((left, right) -> {
            int byLength = Integer.compare(right.text().length(), left.text().length());
            return byLength != 0 ? byLength : left.name().compareTo(right.name());
        });
        return List.copyOf(flat);
    }
}
