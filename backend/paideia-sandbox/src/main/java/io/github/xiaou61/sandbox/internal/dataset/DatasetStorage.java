package io.github.xiaou61.sandbox.internal.dataset;

import java.util.Optional;

/**
 * 数据集内容的存储端口（WORK-035 T-08）。
 *
 * <p><b>为什么不直接用 knowledge 的 `FileStorage`</b>：它在 `knowledge.internal.file` 包里，
 * 跨模块访问对方 internal 包会被 `ModularityTest` 拦下，而且 Maven 依赖方向也会变成
 * sandbox → knowledge，与既有方向（app 装配两者）不一致。
 *
 * <p>端口只要求"按 id 读字节"一件事——数据集内容只读不改，写入走发布流程（同一模块内的
 * 上传服务），删除由作者主动发起。所以这里不需要 put/delete。
 *
 * <p>实现放在 `paideia-app` 的组合根：那里同时认识 sandbox 与 knowledge，正好做桥接。
 */
public interface DatasetStorage {

    /** 读回内容；不存在时返回空（"文件没了"是正常可预期的状态，不是异常）。 */
    Optional<byte[]> read(String storageId);

    /**
     * 空实现：让"没有配置存储"的环境（纯测试、无对象存储的部署）也能起应用。
     *
     * <p>它读什么都是空——私人/社区数据集全部不可用，但内置集不受影响（它们走 classpath）。
     * 这个降级是刻意的：数据集功能不可用比整个服务起不来好得多。
     */
    DatasetStorage NONE = storageId -> Optional.empty();
}
