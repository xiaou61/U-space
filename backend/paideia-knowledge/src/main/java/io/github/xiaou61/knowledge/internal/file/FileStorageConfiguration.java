package io.github.xiaou61.knowledge.internal.file;

import java.net.URI;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * 附件存储装配：**按 {@code paideia.knowledge.storage} 二选一**，并把生效的那个打出来。
 *
 * <p>这就是 WORK-007 说的"换实现 = 改一行配置 + 重启"的落点：两个实现都是完整的 bean 定义，
 * 由 {@code @ConditionalOnProperty} 决定谁来满足 {@link FileStorage}。业务代码始终只认识端口，
 * 一行都不用改——这也是"以后想换随时能换"这句话唯一算数的形态。
 *
 * <p>{@code matchIfMissing = true} 让不配这个键的环境（本地开发、既有测试）**行为与从前完全一致**
 * （本地目录）：升级不会因为缺一个键而改变现有部署的存储位置。
 *
 * <p><b>启动时不做对象存储探活</b>：附件是"用到才读"，启动时连不上不该让整站起不来。
 * 但**配置本身必须 fail fast**——配了 s3 却没给端点或凭据属于配置错误，在这里就报清楚，
 * 而不是等第一次上传时抛一个 SDK 的底层异常。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KnowledgeStorageProperties.class)
public class FileStorageConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FileStorageConfiguration.class);

    @Bean
    @ConditionalOnProperty(name = "paideia.knowledge.storage", havingValue = "local", matchIfMissing = true)
    FileStorage localFileStorage(KnowledgeStorageProperties properties) {
        Path root = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        if (System.getProperty("java.io.tmpdir") != null
                && root.startsWith(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize())) {
            log.warn("""
                    附件目录未显式配置 paideia.knowledge.upload-dir，正在使用系统临时目录：{}
                    临时目录会被系统清理、也不适合备份；部署到任何真实环境前必须显式配置。
                    """, root);
        } else {
            log.info("附件存储：本地目录 {}", root);
        }
        return new LocalFileStorage(root);
    }

    @Bean
    @ConditionalOnProperty(name = "paideia.knowledge.storage", havingValue = "s3")
    S3Client s3Client(KnowledgeStorageProperties properties) {
        KnowledgeStorageProperties.S3 s3 = requireS3Config(properties);
        return S3Client.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(credentials(s3))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(s3.isPathStyle()).build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "paideia.knowledge.storage", havingValue = "s3")
    S3Presigner s3Presigner(KnowledgeStorageProperties properties) {
        KnowledgeStorageProperties.S3 s3 = requireS3Config(properties);
        return S3Presigner.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(credentials(s3))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(s3.isPathStyle()).build())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "paideia.knowledge.storage", havingValue = "s3")
    FileStorage s3FileStorage(S3Client client, S3Presigner presigner, KnowledgeStorageProperties properties) {
        KnowledgeStorageProperties.S3 s3 = requireS3Config(properties);
        if (s3.getRedirectCache().compareTo(s3.getPresignTtl()) >= 0) {
            throw new IllegalStateException(
                    "paideia.knowledge.s3.redirect-cache（" + s3.getRedirectCache() + "）必须小于 presign-ttl（"
                            + s3.getPresignTtl() + "），否则浏览器会拿着已过期的签名去直读");
        }
        // 桶名打出来便于排障；**凭据一个字都不打**
        log.info("附件存储：对象存储 {} 桶 {}", s3.getEndpoint(), s3.getBucket());
        return new S3FileStorage(client, presigner, s3.getBucket(), s3.getPresignTtl());
    }

    private static KnowledgeStorageProperties.S3 requireS3Config(KnowledgeStorageProperties properties) {
        KnowledgeStorageProperties.S3 s3 = properties.getS3();
        if (s3.getEndpoint().isBlank() || s3.getAccessKey().isBlank() || s3.getSecretKey().isBlank()
                || s3.getBucket().isBlank()) {
            throw new IllegalStateException(
                    "paideia.knowledge.storage=s3 需要 paideia.knowledge.s3.{endpoint,bucket,access-key,secret-key} 全部配置"
                            + "（凭据只从环境变量来）");
        }
        return s3;
    }

    private static StaticCredentialsProvider credentials(KnowledgeStorageProperties.S3 s3) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey()));
    }
}
