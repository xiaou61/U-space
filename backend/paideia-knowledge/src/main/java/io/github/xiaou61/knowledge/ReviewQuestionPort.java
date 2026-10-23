package io.github.xiaou61.knowledge;

import java.util.List;
import java.util.Optional;

/**
 * 复习题生成端口（WORK-034 REQ-003）。
 *
 * <p>knowledge 定义、ai 实现；没有 ai 模块或上游失败时返回 {@link Optional#empty()}，
 * 调用方给降级（回到承载条目），不抛异常。
 *
 * <p>答案与解析只进 `items_json`，接口响应里永不出现。
 */
public interface ReviewQuestionPort {

    record Concept(long id, String name, String summary, String carrierTitle) {
    }

    record Question(long conceptId, String stem, List<String> options, int answerIndex,
                    String explanation, String fingerprint) {
    }

    record Result(List<Question> questions, String model, Integer inputTokens, Integer outputTokens) {
    }

    default Optional<Result> generate(long userId, List<Concept> concepts, int perConcept,
                                      java.util.Set<String> previousStems) {
        return Optional.empty();
    }
}