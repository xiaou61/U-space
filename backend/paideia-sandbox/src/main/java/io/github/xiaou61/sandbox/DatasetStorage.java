package io.github.xiaou61.sandbox;

import java.util.Optional;

/**
 * 数据集内容的存储端口（WORK-035 T-08）。
 *
 * <p><b>为什么不直接用 knowledge 的 `FileStorage`</b>：它在 `knowledge.internal.file` 包里，
 * 跨模块访问对方 internal 包会被 `ModularityTest` 拦下，而且 Maven 依赖方向也会变成
 * sandbox → knowledge，与既有方向（app 装配两者）不一致。
 *
 * <p>实现放在 `paideia-app` 的组合根：那里同时认识 sandbox 与 knowledge，正好做桥接。
 */
public interface DatasetStorage {

    /** 读回内容；不存在时返回空（"文件没了"是正常可预期的状态，不是异常）。 */
    Optional<byte[]> read(String storageId);

    /**
     * 存一段内容，返回它的标识。
     *
     * <p>只在上传流程里调用一次——数据集内容不可原地修改（改就是新的一份）。
     */
    String put(byte[] content, String contentType);

    /**
     * 空实现：让"没有配置存储"的环境（纯测试、无对象存储的部署）也能起应用。
     *
     * <p>它读什么都是空、写会抛——私人/社区数据集全部不可用，但内置集不受影响（走 classpath）。
     * 这个降级是刻意的：数据集功能不可用比整个服务起不来好得多。
     */
    DatasetStorage NONE = new DatasetStorage() {
        @Override
        public Optional<byte[]> read(String storageId) {
            return Optional.empty();
        }

        @Override
        public String put(byte[] content, String contentType) {
            throw new UnsupportedOperationException("没有配置数据集存储");
        }
    };
}
