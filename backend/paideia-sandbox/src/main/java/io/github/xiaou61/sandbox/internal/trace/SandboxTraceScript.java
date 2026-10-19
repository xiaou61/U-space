package io.github.xiaou61.sandbox.internal.trace;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * 录制器脚本的来源（WORK-023）。
 *
 * <p>脚本是**类路径常量**（`sandbox/trace.py`），不从请求或磁盘读——它是安全边界的一部分，
 * 与 `DockerCommand` 里那些加固参数同级。用户代码只能作为被 `exec` 的内容，
 * 不能替换、追加或影响录制器本身。
 *
 * <p>为什么不在运行时拼字符串：脚本有 100 多行，拼在 Java 字符串里既难读也容易
 * 因为转义出错（这里真的踩过 `\n` 被写坏）。放进资源文件，由测试断言它确实被打包。
 */
@Component
class SandboxTraceScript {

    private static final String RESOURCE = "/sandbox/trace.py";

    private final String python = load();

    String python() {
        return python;
    }

    private static String load() {
        try (InputStream stream = SandboxTraceScript.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("缺少录制器脚本：" + RESOURCE);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new IllegalStateException("读取录制器脚本失败：" + RESOURCE, failure);
        }
    }
}
