package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 概念候选的召回率（WORK-033 AC-003）。
 *
 * <h2>金集从哪来</h2>
 *
 * <p>`src/test/resources/golden/concept-gold-set.json`，由 `scripts/build-concept-gold-set.mjs`
 * 从**真实课程内容**生成：
 *
 * <ul>
 *   <li>**概念** = 每一课的主题（课程标题 + 该课第一个学习目标作为摘要）。53 个，
 *       来自 `content/courses/{mysql,java,python}` 的 53 课。</li>
 *   <li>**题目** = 课程里的 `quiz` 围栏块，共 159 道。</li>
 *   <li>**标注** = 一道题的"正确概念"就是它**所在那一课的主题**。这不是我的主观判断——
 *       课程结构本身就是作者给的：第 07 课的题目考的就是第 07 课讲的东西。</li>
 * </ul>
 *
 * <h2>标注偏差（必须说清）</h2>
 *
 * <p>**这个标注是按结构推导的，不是人逐题判断的**。它带来两个方向的影响：
 * <ul>
 *   <li><b>偏乐观</b>：题面里往往直接出现该课的核心术语（"索引""事务"），而
 *       {@link QuestionConceptSuggester} 用的正是 bigram 字面重合——两者天然对齐。</li>
 *   <li><b>偏严格</b>：一道题如果实际考的是前面课的内容（跨课复习题），这里仍算它属于
 *       本课，会拉低召回。</li>
 * </ul>
 *
 * <p>所以这个 Recall 是**"按课程结构标注意义上的召回"**，不是"人工精标语料上的召回"。
 * 后者需要独立标注者，见 `testing/report.md` 的说明。
 *
 * <h2>为什么候选池是全部 53 个概念</h2>
 *
 * <p>如果只把本课概念给算法选，Recall 永远 100%——那测不出任何东西。
 * 把 53 个概念全放进去，才是真实场景：算法要在整个知识库里挑出对的那个。
 */
class ConceptRecallTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** AC-003 的门槛。 */
    private static final double REQUIRED_RECALL_AT_3 = 0.80;

    /** 取前几个作为"可展示候选"。 */
    private static final int RECALL_K = 3;

    private static JsonNode goldSet;

    @BeforeAll
    static void loadGoldSet() throws IOException {
        try (InputStream stream = ConceptRecallTest.class.getResourceAsStream(
                "/golden/concept-gold-set.json")) {
            assertThat(stream).as("金集资源缺失：/golden/concept-gold-set.json").isNotNull();
            goldSet = JSON.readTree(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private record Concept(long id, String name, String summary) {
    }

    private record Question(String stem, List<String> options, long correctConceptId) {
    }

    private static List<Concept> concepts() {
        List<Concept> result = new ArrayList<>();
        goldSet.get("concepts").forEach(node -> result.add(
                new Concept(node.get("id").asLong(), node.get("name").asText(),
                        node.path("summary").asText(""))));
        return result;
    }

    private static List<Question> questions() {
        List<Question> result = new ArrayList<>();
        goldSet.get("questions").forEach(node -> {
            List<String> options = new ArrayList<>();
            node.get("options").forEach(option -> options.add(option.asText()));
            result.add(new Question(node.get("stem").asText(), options,
                    node.get("correctConceptId").asLong()));
        });
        return result;
    }

    /** 把金集的概念转成 suggester 的输入形状。 */
    private static List<QuestionConceptSuggester.ConceptText> conceptTexts(List<Concept> concepts) {
        return concepts.stream()
                .map(concept -> new QuestionConceptSuggester.ConceptText(
                        concept.id(), concept.name(), concept.summary()))
                .toList();
    }

    /**
     * AC-003 的现状：**不达标**（实测 Recall@3 ≈ 0.40，要求 0.80）。
     *
     * <p>这条用例断言的是**当前真实行为**而不是 AC 的要求——它记录一个已确认的缺陷，
     * 不假装通过。修好之后它会红，那时把断言改成 AC 的门槛并更新报告。
     *
     * <p>**已确认的原因**（2026-10-23 实测）：{@link QuestionConceptSuggester} 用中文
     * bigram 的字面重合度打分。而题面与概念名的用词几乎不重叠——
     * 题面是"WHERE score = NULL 会返回什么？"，概念名是"SELECT与WHERE"，
     * 共有的 bigram 只有 `WHERE` 一个，分数被稀释到阈值附近。
     * 让它在字面上"看起来像"需要概念名里带足够的题面词汇，而概念名是课程标题。
     *
     * <p>**没有把本课概念从结构加分里"喂"给算法**：`entryConcepts` 与 `neighbours` 都传空。
     * 生产里这两个参数是有的（题目所在条目已关联的概念、概念图邻居），会提升召回——
     * 但那样测出来的就不是"候选算法本身有多准"，而是"结构提示帮了多少忙"。
     */
    @Test
    @DisplayName("AC-003 现状记录：Recall@3 约 0.40（已知缺陷，要求 0.80）")
    void recallAt3CurrentlyBelowThreshold() {
        List<Concept> concepts = concepts();
        List<Question> questions = questions();
        List<QuestionConceptSuggester.ConceptText> texts = conceptTexts(concepts);

        int hit = 0;
        int missingEvidence = 0;
        Map<Long, Integer> missesByConcept = new HashMap<>();

        for (Question question : questions) {
            List<QuestionConceptSuggester.Suggestion> suggestions = QuestionConceptSuggester.suggest(
                    question.stem(), question.options(), texts,
                    Set.of(), Map.of(), RECALL_K);

            boolean found = suggestions.stream()
                    .anyMatch(suggestion -> suggestion.conceptId() == question.correctConceptId());
            if (found) {
                hit++;
            } else {
                missesByConcept.merge(question.correctConceptId(), 1, Integer::sum);
            }
            // 每条候选都必须能说出"命中了哪些词"——空 hits 等于模型编了个 id
            if (suggestions.stream().anyMatch(suggestion -> suggestion.hits().isEmpty())) {
                missingEvidence++;
            }
        }

        double recall = (double) hit / questions.size();
        System.out.printf("[AC-003] 金集 %d 题 / %d 概念，Recall@%d = %.4f（命中 %d）%n",
                questions.size(), concepts.size(), RECALL_K, recall, hit);
        System.out.printf("[AC-003] 无匹配依据的候选条数 = %d%n", missingEvidence);
        if (!missesByConcept.isEmpty()) {
            System.out.println("[AC-003] 未命中的题按概念分布（id→题数）：" + missesByConcept);
        }

        assertThat(missingEvidence)
                .as("每条候选都必须带可读的匹配依据（AC-003 的后半句）")
                .isZero();
        // 记录当前事实：AC 要求 >= 0.80，实际约 0.40。修好之后这里会红，提醒改断言。
        assertThat(recall)
                .as("当前实测 %.4f（金集 %d 题），AC 要求 ≥ %.2f。若这里红了说明算法被改过，"
                                + "请复核 testing/report.md",
                        recall, questions.size(), REQUIRED_RECALL_AT_3)
                .isLessThan(REQUIRED_RECALL_AT_3)
                .isGreaterThan(0.20);
    }

    /**
     * 逆向对照：把候选池打乱后 Recall 必须显著下降。
     *
     * <p>**没有这条，上面那条可能是假通过**——如果 suggester 对任何输入都返回同样的几个
     * 高频概念，而金集里恰好有概念重复出现，那 Recall 会被算高。
     * 这条用"正确答案换成随机概念"来确认度量是真的在区分。
     */
    @Test
    @DisplayName("AC-003 反向验证：正确答案随机化后 Recall 必须显著下降")
    void recallDropsWhenAnswerIsRandomized() {
        List<Concept> concepts = concepts();
        List<Question> questions = questions();
        List<QuestionConceptSuggester.ConceptText> texts = conceptTexts(concepts);

        java.util.Random dice = new java.util.Random(20261023L);
        int randomHit = 0;
        for (Question question : questions) {
            List<QuestionConceptSuggester.Suggestion> suggestions = QuestionConceptSuggester.suggest(
                    question.stem(), question.options(), texts, Set.of(), Map.of(), RECALL_K);
            long randomAnswer = concepts.get(dice.nextInt(concepts.size())).id();
            if (suggestions.stream().anyMatch(s -> s.conceptId() == randomAnswer)) {
                randomHit++;
            }
        }
        double randomRecall = (double) randomHit / questions.size();
        // 纯随机命中率的上界：前 3 个候选里的概念数 / 概念总数，约 3/53 ≈ 5.7%
        System.out.printf("[AC-003-reverse] 随机答对率 = %.4f%n", randomRecall);

        assertThat(randomRecall)
                .as("随机答对率 %.4f 应该远低于 0.80——否则说明召回不是真的在起作用", randomRecall)
                .isLessThan(0.30);
    }
}
