package io.github.xiaou61.ai.internal.camp;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.ai.ResumeData;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 技能画像（WORK-029 TASK-002）。
 *
 * <p>最要紧的一条是最后那个「不泄露 PII」的用例：它把姓名、电话、邮箱、公司、学校全写进
 * 夹具，然后断言 {@code promptBlock()} 里一个都不出现。把 {@code promptBlock()} 改成拼接
 * {@code data.basics()} 就会红——这就是本项的安全边界。
 */
class SkillDigestTest {

    private static ResumeData resume(List<String> skillItems, List<String> projectBullets,
                                     List<String> workBullets) {
        return new ResumeData(
                new ResumeData.Basics("张三", "13800001111", "zhangsan@example.com", "杭州", List.of()),
                new ResumeData.Intent("Java 后端", "杭州", ""),
                List.of(new ResumeData.Education("浙江大学", "本科", "计算机", "2019", "2023", List.of())),
                List.of(new ResumeData.Work("某真实公司名", "后端工程师", "2023.07", "至今",
                        "负责订单系统", workBullets)),
                List.of(new ResumeData.Project("某真实项目名", "主力开发", "2024", projectBullets)),
                skillItems.isEmpty() ? List.of() : List.of(new ResumeData.SkillGroup("技术栈", skillItems)),
                List.of(), "");
    }

    @Test
    @DisplayName("技能区里的每一项都成为 term，只出现在一个 section 时覆盖度为 1/3")
    void skillItemsBecomeTerms() {
        SkillDigest.Digest digest = SkillDigest.digest(
                resume(List.of("Java", "Spring Boot", "MySQL"), List.of(), List.of()));

        assertThat(digest.terms()).containsKeys("Java", "Spring Boot", "MySQL");
        assertThat(digest.coverage("Java")).isEqualTo(1.0 / 3, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(digest.terms().get("Java").sections()).isEqualTo(Set.of(SkillDigest.SKILLS));
    }

    @Test
    @DisplayName("正文里反复出现的技术词按 section 累加覆盖度")
    void repeatedTermAccumulatesSections() {
        SkillDigest.Digest digest = SkillDigest.digest(resume(
                List.of("Java"),
                List.of("用 Redis 做缓存，QPS 从 1200 提到 3000"),
                List.of("Redis 集群扩到 6 节点")));

        assertThat(digest.terms().get("Redis").sections())
                .containsExactlyInAnyOrder(SkillDigest.PROJECTS, SkillDigest.WORK);
        assertThat(digest.coverage("Redis")).isEqualTo(2.0 / 3, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("三个 section 都出现的 term 覆盖度封顶 1")
    void allSectionsMaxesCoverage() {
        SkillDigest.Digest digest = SkillDigest.digest(resume(
                List.of("Redis"), List.of("Redis 缓存"), List.of("Redis 主从")));
        assertThat(digest.coverage("Redis")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("只有姓名电话的空简历 isEmpty")
    void emptyResumeIsEmpty() {
        SkillDigest.Digest digest = SkillDigest.digest(resume(List.of(), List.of(), List.of()));
        assertThat(digest.isEmpty()).isTrue();
        assertThat(digest.coverage("Java")).isZero();
    }

    @Test
    @DisplayName("别名不会被更短的同族词吃掉：Spring Boot 与 JavaScript 各算各的")
    void aliasesDoNotSwallowEachOther() {
        SkillDigest.Digest digest = SkillDigest.digest(resume(
                List.of("Spring Boot", "JavaScript"), List.of(), List.of("用 Java 写服务")));

        assertThat(digest.terms()).containsKey("Spring Boot");
        assertThat(digest.terms()).containsKey("JavaScript");
        assertThat(digest.terms()).containsKey("Java");
        // Spring 不该因为 "Spring Boot" 而额外命中（占位已消费）
        assertThat(digest.terms().getOrDefault("Spring", null)).isNull();
    }

    @Test
    @DisplayName("promptBlock 只含技术词，姓名电话邮箱公司学校一个都不出现")
    void promptBlockCarriesNoPii() {
        SkillDigest.Digest digest = SkillDigest.digest(resume(
                List.of("Java", "MySQL"),
                List.of("在浙江大学的项目里用 Redis"),
                List.of("在某真实公司名负责 Kafka")));
        String block = digest.promptBlock();

        assertThat(block).contains("Java").contains("MySQL").contains("Redis").contains("Kafka");
        assertThat(block)
                .doesNotContain("张三")
                .doesNotContain("13800001111")
                .doesNotContain("zhangsan@example.com")
                .doesNotContain("某真实公司名")
                .doesNotContain("浙江大学")
                .doesNotContain("某真实项目名");
    }

    @Test
    @DisplayName("null 简历不抛错，返回空画像")
    void nullResumeIsEmpty() {
        assertThat(SkillDigest.digest(null).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("HTTP 与 HTTPS 是两个词，短词不吞长词")
    void httpAndHttpsAreDistinct() {
        SkillDigest.Digest digest = SkillDigest.digest(resume(List.of(), List.of(), List.of("配置 HTTPS 证书")));
        assertThat(digest.terms()).containsKey("HTTPS");
        assertThat(digest.terms()).doesNotContainKey("HTTP");
    }
}
