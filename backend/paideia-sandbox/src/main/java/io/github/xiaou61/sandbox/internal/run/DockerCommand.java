package io.github.xiaou61.sandbox.internal.run;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 构造 `docker run` 命令行（纯函数，WORK-015）。
 *
 * <p><b>为什么单独一个类而不是塞在 runner 里</b>：这些参数是**安全边界**，必须能被逐条断言。
 * 一个"看起来对"的命令行少写一个 `--cap-drop` 就等于没隔离，而那在本地根本测不出来。
 * 所以这里只做字符串拼装，由 {@code DockerCommandTest} 把每一条加固标志钉死。
 *
 * <p>另外三条纪律：
 * <ul>
 *   <li>**没有 `-v` / `--mount`**：代码通过 stdin 进容器，宿主文件系统不出现。</li>
 *   <li>**镜像来自配置，不来自请求**：用户输入永远进不了命令行。</li>
 *   <li>`--read-only` 之后仍要可写 `/tmp`：`javac` 得写 class 文件，所以给一个 64MB 的 tmpfs。</li>
 * </ul>
 */
final class DockerCommand {

    /**
     * 容器内执行脚本：从 stdin 落盘，再编译/运行。
     *
     * <p><b>分帧协议</b>：先读一行「源码字节数」，再读恰好那么多字节作为源码，剩下的才是
     * 程序的标准输入。不能简单 `cat > file` 把代码和 stdin 一起倒进去——`cat` 会一直吃到 EOF，
     * 用户传入的 stdin 会被追加进源码（变成莫名其妙的编译错误），而程序自己拿到的是 EOF。
     *
     * <p>脚本本身是常量，不含用户内容。
     */
    private static final String JAVA_SCRIPT = """
            read -r size
            dd bs=1 count="$size" of=/tmp/Main.java 2>/dev/null
            cd /tmp && javac Main.java && java Main""";
    private static final String PYTHON_SCRIPT = """
            read -r size
            dd bs=1 count="$size" of=/tmp/main.py 2>/dev/null
            cd /tmp && python main.py""";

    private DockerCommand() {
    }

    /**
     * @param language 已校验过的语言标识（java / python）
     * @param props    资源与运行时配置
     * @param cidFile  容器 id 落到哪个文件（超时兜底删容器用）
     */
    static List<String> of(String language, SandboxProperties props, String cidFile) {
        String image = props.imageFor(language);
        if (image == null) {
            throw new IllegalArgumentException("不支持的语言：" + language);
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);

        List<String> command = new ArrayList<>(List.of(
                "docker", "run",
                "--rm",
                "-i",
                // ── 网络：完全断开。任何出网都可能被当成代理、扫描器或矿池客户端 ──
                "--network=none",
                // ── 文件系统：根只读，唯一的可写处是一个有上限的 tmpfs ──
                "--read-only",
                "--tmpfs", "/tmp:rw,size=64m,exec",
                // ── 身份与权限：非 root、cap 全丢、禁止提权 ──
                "--user", "65534:65534",
                "--cap-drop=ALL",
                "--security-opt=no-new-privileges",
                // ── 资源上限 ──
                "--memory=" + props.getMemory(),
                "--memory-swap=" + props.getMemory(),
                "--cpus=" + props.getCpus(),
                "--pids-limit=" + props.getPidsLimit(),
                "--ulimit", "nofile=64",
                "--ulimit", "nproc=64",
                // 停止超时 0：超时后立即 SIGKILL，不给程序自己收尾的机会
                "--stop-timeout=0",
                // 记录容器 id：宿主上的 docker CLI 被强杀后容器仍归 daemon 管，
                // `--rm` 只在容器自己退出时生效。调用方据此在超时分支里补一刀 docker rm -f。
                "--cidfile", cidFile,
                // 时区固定：输出不随部署机漂移
                "-e", "TZ=UTC",
                // 入口：脚本从 stdin 读代码
                "--entrypoint", "sh"
        ));

        // 可选：切到 gVisor。空字符串 = 不传，走 Docker 默认 runtime
        if (props.getRuntime() != null && !props.getRuntime().isBlank()) {
            command.add("--runtime=" + props.getRuntime().trim());
        }

        command.add(image);
        command.add("-c");
        command.add("java".equals(normalized) ? JAVA_SCRIPT : PYTHON_SCRIPT);
        return List.copyOf(command);
    }

    /** 容器内的脚本常量（测试与文档共用，避免两处各写一份）。 */
    static String scriptFor(String language) {
        return "java".equals(language.trim().toLowerCase(Locale.ROOT)) ? JAVA_SCRIPT : PYTHON_SCRIPT;
    }
}
