package io.github.xiaou61.knowledge.internal.file;

import java.time.Duration;
import java.util.Optional;

/**
 * 附件存储端口。
 *
 * <p>业务代码只依赖它，不认识磁盘、S3 或任何具体实现——本版只有 {@link LocalFileStorage}
 * 一个实现（服务器本地目录）。将来换对象存储时替换实现，业务代码不动，这也是用户选
 * "现在只用本地、但要可扩展"时要的那个接缝。
 *
 * <p>{@code id} 由实现生成（本版是 UUID）并作为**不透明标识**使用：调用方不要解析它、
 * 也不要假定它等同于磁盘上的文件名。返回端点免鉴权，所以实现必须保证 id 不可枚举。
 */
public interface FileStorage {

    /**
     * 存一段内容，返回它的标识。
     *
     * <p>{@code contentType} 是**嗅探得到的**类型，不是客户端声明的——实现可以拿它去做
     * 对象元数据（S3 就需要），也可以忽略。
     */
    String put(byte[] content, String contentType);

    /** 读回内容；不存在时返回空（"文件没了"是正常可预期的状态，不是异常）。 */
    Optional<byte[]> read(String id);

    /** 删除；不存在时返回 false。用于"插表失败后回滚刚写的文件"。 */
    boolean delete(String id);

    /**
     * 能直读就给一个有签名的 URL，不能就给空（调用方自己把字节流出去）。
     *
     * <p><b>写成 default 是刻意的</b>：端口对实现的要求仍然只有上面三个必答项，
     * "能不能直读"在这里是**能力**而不是**义务**——本地磁盘没有"直读"这个东西，
     * 它返回空，调用方照旧流字节，行为与本项之前一模一样。
     *
     * <p>对象存储实现返回的是**带签名、有有效期**的 URL：拿到它就等于拿到那段内容的读取权，
     * 所以调用方不要把它写进日志、也不要长期缓存（有效期由调用方按场景给）。
     */
    default Optional<String> presignedGetUrl(String id, Duration ttl) {
        return Optional.empty();
    }
}
