package io.github.xiaou61.sandbox.internal.run;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 能力探测（WORK-015）：启动时问一次 Docker，之后只读缓存。
 *
 * <p><b>为什么不让探测失败阻止启动</b>：本地开发、CI、以及"只想读文章"的部署都不该因为
 * Docker 不在而起不来。能力不可用的表现是"运行按钮不出现 + 接口 503"，不是一个起不来的服务。
 *
 * <p>只探一次、不重试：Docker 装好之后重启应用即可。周期重探要多一个调度与状态机，收益很低。
 */
@Component
class SandboxCapability {

    private static final Logger log = LoggerFactory.getLogger(SandboxCapability.class);

    private final SandboxProperties properties;
    private volatile boolean ready;
    private volatile String reason;

    SandboxCapability(SandboxProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void probe() {
        if (!properties.isEnabled()) {
            ready = false;
            reason = "代码执行已关闭";
            return;
        }
        try {
            int exit = run(List.of("docker", "version", "--format", "{{.Server.Version}}"), 3);
            if (exit != 0) {
                ready = false;
                reason = "Docker 不可用（docker version 返回 " + exit + "）";
                log.warn("沙箱不可用：{}", reason);
                return;
            }
            for (String image : properties.images()) {
                int inspect = run(List.of("docker", "image", "inspect", image), 5);
                if (inspect != 0) {
                    ready = false;
                    reason = "镜像尚未拉取：" + image;
                    log.warn("沙箱不可用：{}", reason);
                    return;
                }
            }
            ready = true;
            reason = null;
            log.info("沙箱就绪：runtime={} images={}",
                    properties.getRuntime().isBlank() ? "docker-default" : properties.getRuntime(),
                    properties.images());
        } catch (IOException cause) {
            ready = false;
            reason = "无法执行 docker 命令（" + cause.getMessage() + "）";
            log.warn("沙箱不可用：{}", reason);
        }
    }

    boolean ready() {
        return ready;
    }

    String reason() {
        return reason;
    }

    private static int run(List<String> command, int timeoutSeconds) throws IOException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return -1;
            }
            return process.exitValue();
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return -1;
        }
    }
}
