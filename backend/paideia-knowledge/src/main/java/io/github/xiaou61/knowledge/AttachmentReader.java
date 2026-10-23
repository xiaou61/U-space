package io.github.xiaou61.knowledge;

import java.util.Optional;

/**
 * 附件的读写（WORK-035 T-09）。
 *
 * <p><b>为什么单独一个接口而不是塞进 {@link KnowledgeApi}</b>：`KnowledgeApi` 是知识库的
 * 业务契约（条目、分类、自评）。加"按 id 读写任意字节"会把它变成通用文件服务，
 * 而那是两件不相关的事。
 *
 * <p><b>为什么需要这个接口</b>：SQL 数据集的内容也存在同一个对象存储里（WORK-007 的
 * `FileStorage`）。`FileStorage` 在 `knowledge.internal.file`，sandbox 直接引会被
 * `ModularityTest` 拦下。这个接口是那道墙上的一个窄门。
 *
 * <p>**写入只给 put**：没有 delete。数据集内容不可原地修改（改就是新的一份），
 * 删除由作者主动发起时走同模块的清理逻辑——这里不该成为"谁能删别人附件"的入口。
 */
public interface AttachmentReader {

    /** 读回内容；不存在时返回空（"文件没了"是正常可预期的状态，不是异常）。 */
    Optional<byte[]> read(String fileId);

    /**
     * 存一段内容，返回它的标识。
     *
     * <p>`contentType` 是**嗅探得到的**类型，不是客户端声明的。
     */
    String put(byte[] content, String contentType);
}
