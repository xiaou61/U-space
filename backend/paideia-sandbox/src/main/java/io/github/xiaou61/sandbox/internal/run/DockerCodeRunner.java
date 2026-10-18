package io.github.xiaou61.sandbox.internal.run;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用一次性容器执行代码（WORK-015）。
 *
 * <p><b>为什么是 `docker run` 子进程而不是 runner 服务</b>：当前部署是 systemd + 裸 jar，
 * 宿主已有 Docker；`docker run` 本身就是独立进程，崩掉不影响 JVM。少一个部署单元、少一套
 * 内网鉴权。**已知限制**：应用一旦容器化，这条路就没有 socket 可用，届时必须改成独立 runner
 * （见 design「已知限制」）。
 *
 * <p><b>三条实现纪律</b>：
 * <ul>
 *   <li>**输出必须边跑边读**：不读的话管道写满 64KB 就会把子进程卡死，表现为"莫名超时"。</li>
 *   <li>**超时用 `destroyForcibly`**：先温和再强杀要两段等待，而这里没有"优雅退出"的语义。</li>
 *   <li>**代码只走 stdin**：不进命令行（否则会出现在 `ps` 与 shell 历史里），也不落宿主磁盘。</li>
 * </ul>
 */
@Component
class DockerCodeRunner implements CodeRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerCodeRunner.class);

    private final SandboxProperties properties;
    private final SandboxCapability capability;

    DockerCodeRunner(SandboxProperties properties, SandboxCapability capability) {
        this.properties = properties;
        this.capability = capability;
    }

    @Override
    public boolean ready() {
        return properties.isEnabled() && capability.ready();
    }

    @Override
    public String unavailableReason() {
        if (!properties.isEnabled()) {
            return "代码执行已关闭";
        }
        return capability.reason();
    }

    @Override
    public CodeRun.Result run(CodeRun.Request request) {
        List<String> command = DockerCommand.of(request.language(), properties);
        long started = System.nanoTime();
        Process process;
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException cause) {
            throw new SandboxUnavailableException("启动容器失败：" + cause.getMessage(), cause);
        }

        // stdin 写完就关：脚本要读到 EOF 才会往下执行
        try (OutputStream stdin = process.getOutputStream()) {
            String input = request.code() + "\n";
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
            if (request.stdin() != null && !request.stdin().isEmpty()) {
                stdin.write(request.stdin().getBytes(StandardCharsets.UTF_8));
            }
            stdin.flush();
        } catch (IOException cause) {
            // 容器提前退出（例如镜像缺失）时写 stdin 会失败；继续读输出，错误会体现在 stderr
            log.debug("写入容器 stdin 失败（容器可能已退出）：{}", cause.getMessage());
        }

        // 两个流同时读：不读的那个会把子进程卡在 write 上
        BoundedBuffer stdout = new BoundedBuffer(properties.getMaxOutputBytes());
        BoundedBuffer stderr = new BoundedBuffer(properties.getMaxOutputBytes());
        Thread outReader = streamReader(process.getInputStream(), stdout);
        Thread errReader = streamReader(process.getErrorStream(), stderr);

        boolean finished;
        try {
            finished = process.waitFor(properties.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new SandboxUnavailableException("执行被中断", cause);
        }
        if (!finished) {
            // 强杀：容器是 --rm 的，docker CLI 被杀后容器会随 --stop-timeout=0 立即终止
            process.destroyForcibly();
        }

        joinQuietly(outReader);
        joinQuietly(errReader);
        long durationMs = (System.nanoTime() - started) / 1_000_000;

        int exitCode = finished ? process.exitValue() : -1;
        String out = stdout.text();
        String err = stderr.text();
        boolean oom = !finished && looksLikeOom(exitCode, err, out);
        log.info("sandbox run language={} finished={} exit={} durationMs={} outBytes={}",
                request.language(), finished, exitCode, durationMs,
                out.getBytes(StandardCharsets.UTF_8).length);

        return new CodeRun.Result(
                out,
                err,
                exitCode,
                durationMs,
                !finished,
                oom || exitCode == 137 && err.contains("Killed"),
                stdout.truncated() || stderr.truncated());
    }

    /**
     * 区分"超时"与"被 OOM 杀掉"。
     *
     * <p>两者都是非 0 退出，但给用户的提示完全不同：一个是"你的循环没停"，另一个是"你的内存用超了"。
     * 容器被 cgroup OOM killer 杀时，退出码通常是 137，且 stderr 里会有 `Killed`；
     * 而普通超时是我们主动 kill，**不会有** Killed 字样。
     */
    private static boolean looksLikeOom(int exitCode, String err, String out) {
        return exitCode == 137 && (err.contains("Killed") || out.contains("Killed"));
    }

    private static Thread streamReader(InputStream stream, BoundedBuffer sink) {
        Thread thread = new Thread(() -> {
            byte[] chunk = new byte[8192];
            try (stream) {
                int read;
                while ((read = stream.read(chunk)) != -1) {
                    sink.write(chunk, read);
                }
            } catch (IOException ignored) {
                // 进程被杀时读端会异常结束，这是预期路径
            }
        }, "sandbox-stream");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void joinQuietly(Thread thread) {
        try {
            thread.join(TimeUnit.SECONDS.toMillis(2));
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
        }
    }

    /** 有上限的输出缓冲：超限后继续读（排空管道）但不再存。 */
    static final class BoundedBuffer {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final int limit;
        private boolean truncated;

        BoundedBuffer(int limit) {
            this.limit = limit;
        }

        synchronized void write(byte[] chunk, int length) {
            int room = limit - buffer.size();
            if (room <= 0) {
                truncated = true;
                return;
            }
            if (length > room) {
                buffer.write(chunk, 0, room);
                truncated = true;
                return;
            }
            buffer.write(chunk, 0, length);
        }

        synchronized String text() {
            return buffer.toString(StandardCharsets.UTF_8);
        }

        synchronized boolean truncated() {
            return truncated;
        }
    }
}
