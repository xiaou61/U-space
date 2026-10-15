package io.github.xiaou61.knowledge.internal.file;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * "换实现 = 改一行配置"这句话的机器可验证部分。
 *
 * <p>用 {@code ApplicationContextRunner} 而不是 {@code @SpringBootTest}：这里要验的是**装配条件**
 * （配 local 给 local、配 s3 给 s3、s3 缺凭据要 fail fast），不是整个应用。跑一个真实上下文要
 * 连数据库、要起 Flyway，而换个属性又会因为"上下文按配置键缓存"再起一套——那笔开销在这里
 * 完全没必要付。
 */
class FileStorageConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FileStorageConfiguration.class));

    @Test
    void defaultsToLocalStorage() {
        // 不配 storage：行为必须与从前完全一致（既有部署升级后不该悄悄换地方）
        runner.run(context -> assertThat(context.getBean(FileStorage.class)).isInstanceOf(LocalFileStorage.class));
    }

    @Test
    void explicitLocalStillSelectsLocal() {
        runner.withPropertyValues("paideia.knowledge.storage=local")
                .run(context -> assertThat(context.getBean(FileStorage.class)).isInstanceOf(LocalFileStorage.class));
    }

    @Test
    void s3SelectionWiresTheObjectStorageImplementation() {
        runner.withPropertyValues(
                        "paideia.knowledge.storage=s3",
                        "paideia.knowledge.s3.endpoint=http://127.0.0.1:9000",
                        "paideia.knowledge.s3.bucket=paideia-attachments",
                        "paideia.knowledge.s3.access-key=test-key",
                        "paideia.knowledge.s3.secret-key=test-secret")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(FileStorage.class)).isInstanceOf(S3FileStorage.class);
                    // 本地实现与 S3 客户端不该同时存在：两个实现并存会让"到底在写哪"变得不确定
                    assertThat(context).doesNotHaveBean(LocalFileStorage.class);
                    assertThat(context).hasSingleBean(FileStorage.class);
                });
    }

    @Test
    void missingS3CredentialsFailFast() {
        // 配了 s3 却没给凭据属于配置错误：必须在启动时报清楚，而不是等第一次上传抛 SDK 异常
        runner.withPropertyValues("paideia.knowledge.storage=s3", "paideia.knowledge.s3.endpoint=http://127.0.0.1:9000")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void redirectCacheMustBeShorterThanPresignTtl() {
        // 302 的缓存时长若大于等于签名有效期，浏览器会拿着过期签名去直读——装配期就要拦住
        runner.withPropertyValues(
                        "paideia.knowledge.storage=s3",
                        "paideia.knowledge.s3.endpoint=http://127.0.0.1:9000",
                        "paideia.knowledge.s3.access-key=k",
                        "paideia.knowledge.s3.secret-key=s",
                        "paideia.knowledge.s3.presign-ttl=1h",
                        "paideia.knowledge.s3.redirect-cache=2h")
                .run(context -> assertThat(context).hasFailed());
    }
}
