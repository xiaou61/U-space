package io.github.xiaou61.sandbox.internal.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 命令构造的**安全断言**（WORK-015 AC-002 / AC-008）。
 *
 * <p>这一组用例的存在理由：这些参数就是隔离边界。少写一个 `--cap-drop=ALL`，代码仍然能跑、
 * 测试也可能全绿，只是隔离没了——而这种缺失在本地完全观察不到。所以每条加固标志都单独断言，
 * 将来有人"顺手简化一下命令"时会被这里拦住。
 */
class DockerCommandTest {

    private static SandboxProperties props() {
        SandboxProperties properties = new SandboxProperties();
        properties.setJavaImage("eclipse-temurin:21-jdk-alpine");
        properties.setPythonImage("python:3.12-alpine");
        return properties;
    }

    /** 每条都是安全边界：断的是"在不在"，不是"位置对不对"。 */
    private static final List<String> MANDATORY = List.of(
            "--rm",
            "--network=none",
            "--read-only",
            "--user", "65534:65534",
            "--cap-drop=ALL",
            "--security-opt=no-new-privileges",
            "--memory-swap=256m",
            "--pids-limit=64",
            "--stop-timeout=0");

    private static void assertHardened(List<String> command) {
        for (String flag : MANDATORY) {
            assertThat(command)
                    .as("加固参数 %s 必须在命令行里——它缺失时程序照样能跑，只是隔离没了", flag)
                    .contains(flag);
        }
        assertThat(command).contains("--memory=256m", "--cpus=0.5", "--network=none");

        // 绝对不能有的：任何形式的宿主挂载
        assertThat(command)
                .as("不能把宿主目录挂进容器")
                .noneMatch(item -> item.equals("-v") || item.startsWith("--volume") || item.startsWith("--mount"));
    }

    @Test
    void java命令包含全部加固参数且不挂载宿主() {
        List<String> command = DockerCommand.of("java", props(), "/tmp/x.cid");
        assertHardened(command);
        assertThat(command).contains("eclipse-temurin:21-jdk-alpine");
        assertThat(command.get(command.size() - 1)).isEqualTo(DockerCommand.scriptFor("java"));
        assertThat(command).contains("--entrypoint", "sh", "-c");
    }

    @Test
    void python命令同样加固() {
        List<String> command = DockerCommand.of("python", props(), "/tmp/x.cid");
        assertHardened(command);
        assertThat(command).contains("python:3.12-alpine");
        assertThat(command.get(command.size() - 1)).isEqualTo(DockerCommand.scriptFor("python"));
    }

    @Test
    void 配置了runtime才加runtime参数() {
        SandboxProperties plain = props();
        assertThat(DockerCommand.of("java", plain, "/tmp/x.cid")).noneMatch(item -> item.startsWith("--runtime"));

        SandboxProperties gvisor = props();
        gvisor.setRuntime("runsc");
        assertThat(DockerCommand.of("java", gvisor, "/tmp/x.cid")).contains("--runtime=runsc");
    }

    @Test
    void 语言标识大小写不敏感但不支持的语言被拒() {
        assertThat(DockerCommand.of("JAVA", props(), "/tmp/x.cid")).contains(DockerCommand.scriptFor("java"));
        assertThatThrownBy(() -> DockerCommand.of("ruby", props(), "/tmp/x.cid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的语言");
    }

    @Test
    void 语言白名单只有java与python() {
        SandboxProperties properties = props();
        assertThat(properties.languages()).containsExactlyInAnyOrder("java", "python");
        assertThat(properties.isSupported("java")).isTrue();
        assertThat(properties.isSupported("PHP")).isFalse();
        assertThat(properties.imageFor("ruby")).isNull();
    }

    @Test
    void 内存与cpus可配置且真的进了命令行() {
        SandboxProperties properties = props();
        properties.setMemory("128m");
        properties.setCpus("0.25");
        properties.setPidsLimit(32);
        List<String> command = DockerCommand.of("python", properties, "/tmp/x.cid");
        assertThat(command).contains("--memory=128m", "--memory-swap=128m", "--cpus=0.25", "--pids-limit=32");
    }
}
