package io.github.xiaou61.sandbox;

import java.util.List;

/**
 * 用模型在数据集上现场出题（WORK-035 T-12）。
 *
 * <p>与 `QuestionTaggingPort` 同一模式：sandbox 定义端口，ai 模块实现；没有 ai 模块时
 * 走默认空实现，工作台只是"没有 AI 出题"，不影响手动写 SQL。
 *
 * <p><b>题不落库</b>：出题是当场生成、当场用完（与 WORK-032「不建题库」一致）。
 *
 * <p><b>为什么端口要带 datasetSchema 而不是 key</b>：ai 模块不认识数据集、也不该认识——
 * 它只需要知道"这个库长什么样"来出题。把 key 交给它等于让它去查库，那是越权。
 */
public interface SqlQuestionPort {

    /**
     * @param datasetSchema 数据集的表结构 JSON（与前端补全同一份）
     * @param goal          可选的出题方向（用户填的），为空时由模型自己挑
     */
    record Request(String datasetKey, String datasetSchema, String goal) {
    }

    /**
     * 一道题。
     *
     * @param stem       题面
     * @param referenceSql 参考解——**调用方必须真跑一遍**再拿结果当期望值
     * @param criteria   判定口径（要哪几列、要不要排序、NULL 怎么算）
     * @param explain    讲解
     */
    record Question(String stem, String referenceSql, String criteria, String explain) {
    }

    /** 失败时返回空列表（没有凭据、上游超时、JSON 坏掉都算）。 */
    record Result(List<Question> questions, String model, String promptVersion) {
    }

    default Result generate(Request request) {
        return new Result(List.of(), null, null);
    }
}
