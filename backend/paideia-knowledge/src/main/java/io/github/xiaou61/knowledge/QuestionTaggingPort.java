package io.github.xiaou61.knowledge;

import java.util.List;

/**
 * 用模型给题目生成概念候选（WORK-033 REQ-010）。
 *
 * <p>与 {@link MasteryEvidence} 同一模式：knowledge 定义端口，ai 模块实现；没有 ai 模块时
 * 走默认空实现，管理端只是"没有 AI 候选"，不影响其它功能。
 *
 * <p>**只产候选，不落库**：作者确认后才通过标注接口写入。这样模型编的 id 永远不会直接生效。
 */
public interface QuestionTaggingPort {

    record Question(String questionKey, String stem, List<String> options) {
    }

    record Concept(long id, String name, String summary) {
    }

    record Request(List<Question> questions, List<Concept> concepts) {
    }

    record Suggestion(String questionKey, long conceptId) {
    }

    record Result(List<Suggestion> suggestions, String model, String promptVersion) {
    }

    default Result tag(Request request) {
        return new Result(List.of(), null, null);
    }
}