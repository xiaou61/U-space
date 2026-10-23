package io.github.xiaou61.sandbox;

/**
 * 用模型生成一个数据集（DDL + INSERT）（WORK-035 T-13）。
 *
 * <p><b>生成的 SQL 必须先干跑通过才允许保存</b>——调用方负责，端口只负责生成。
 * 不跑的话用户会拿到一个永远起不来的数据集，而发现时机是别人用它做题的时候。
 *
 * <p><b>默认私有</b>：生成的数据集只有作者能看，发布要走主动操作。
 */
public interface SqlDatasetGeneratorPort {

    /**
     * @param topic 用户描述想要什么数据（如"一个电商订单库，含客户、订单、商品"）
     */
    record Request(String topic) {
    }

    /**
     * @param sql 完整的建表与插数语句
     * @param name 建议的数据集名字
     */
    record Result(String sql, String name, String model, String promptVersion) {
    }

    default Result generate(Request request) {
        return new Result(null, null, null, null);
    }
}
