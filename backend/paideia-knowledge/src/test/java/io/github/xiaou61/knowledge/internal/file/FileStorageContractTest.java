package io.github.xiaou61.knowledge.internal.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * 附件存储端口的**契约测试**：同一批断言，跑在两种实现上。
 *
 * <p>本项（WORK-007）的验收核心就是这一个类。"以后想换实现随时能换"这句话，只有在
 * **同一份不针对实现写的断言在两个实现上都过**时才成立；否则那只是句声明。
 *
 * <p>本地实现用临时目录，永远跑；对象存储实现需要真的连得上，所以以
 * {@code PAIDEIA_KNOWLEDGE_S3_ENDPOINT} 为开关**跳过而不是失败**（与数据库那套集成测试
 * 同一做法：本机没有隧道时不该红）。
 *
 * <p>断言里刻意包含几条"形状"约束（id 必须是 UUID、非 UUID 一律当不存在）：读端点免鉴权，
 * **id 不可枚举是唯一的安全前提**，换实现时最容易丢的就是它，所以它必须被钉在这份共用断言里。
 */
class FileStorageContractTest {

    private static final byte[] CONTENT = "契约测试内容——两种实现必须给出同样的行为".getBytes(StandardCharsets.UTF_8);
    private static final String CONTENT_TYPE = "text/plain";

    @Test
    void localStorageHonoursTheContract(@TempDir Path directory) {
        assertContract(new LocalFileStorage(directory), "本地实现");
        // 本地磁盘没有"直读"这个能力：端口的能力方法返回空，调用方照旧流字节
        FileStorage storage = new LocalFileStorage(directory);
        String id = storage.put(CONTENT, CONTENT_TYPE);
        assertThat(storage.presignedGetUrl(id, Duration.ofMinutes(5)))
                .as("本地实现不该声称自己能给出直读 URL")
                .isEmpty();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "PAIDEIA_KNOWLEDGE_S3_ENDPOINT", matches = ".+")
    void objectStorageHonoursTheSameContract() throws Exception {
        String endpoint = System.getenv("PAIDEIA_KNOWLEDGE_S3_ENDPOINT");
        String bucket = Optional.ofNullable(System.getenv("PAIDEIA_KNOWLEDGE_S3_BUCKET")).orElse("paideia-attachments");
        var credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(
                System.getenv("PAIDEIA_KNOWLEDGE_S3_ACCESS_KEY"), System.getenv("PAIDEIA_KNOWLEDGE_S3_SECRET_KEY")));
        S3Configuration pathStyle = S3Configuration.builder().pathStyleAccessEnabled(true).build();

        try (S3Client client = S3Client.builder()
                        .endpointOverride(URI.create(endpoint))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(credentials)
                        .serviceConfiguration(pathStyle)
                        .build();
                S3Presigner presigner = S3Presigner.builder()
                        .endpointOverride(URI.create(endpoint))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(credentials)
                        .serviceConfiguration(pathStyle)
                        .build()) {
            ensureBucket(client, bucket);
            FileStorage storage = new S3FileStorage(client, presigner, bucket, Duration.ofMinutes(10));

            assertContract(storage, "对象存储实现");

            // 直读：URL 指向对象存储本身，且**真的能取回内容**（不只看形状）
            String id = storage.put(CONTENT, CONTENT_TYPE);
            String url = storage.presignedGetUrl(id, Duration.ofMinutes(10)).orElseThrow();
            assertThat(url).startsWith(endpoint).contains(bucket).contains(id).contains("X-Amz-Signature");
            try (HttpClient http = HttpClient.newHttpClient()) {
                HttpResponse<byte[]> response = http.send(
                        HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
                assertThat(response.statusCode()).as("预签名 URL 应当能直接取到内容").isEqualTo(200);
                assertThat(response.body()).isEqualTo(CONTENT);
            }
            storage.delete(id);
        }
    }

    /** 两种实现都必须满足的行为。**这段不看实现，只看端口**。 */
    private static void assertContract(FileStorage storage, String label) {
        // 1) put 返回的 id 是 UUID 形状（不可枚举的前提）
        String id = storage.put(CONTENT, CONTENT_TYPE);
        assertThat(id).as("%s：id 必须是 UUID 文本", label).hasSize(36);
        assertThat(UUID.fromString(id)).isNotNull();

        // 2) read 拿回一模一样的字节
        assertThat(storage.read(id)).as("%s：读回内容要与写入一致", label).contains(CONTENT);

        // 3) 不存在的 id → 空（"文件没了"是可预期状态，不是异常）
        assertThat(storage.read(UUID.randomUUID().toString()))
                .as("%s：不存在的对象应当返回空而不是抛异常", label)
                .isEmpty();

        // 4) 形状不对的 id 一律当不存在（挡住任何"拼接路径/枚举"的可能）
        assertThat(storage.read("../../etc/passwd")).as("%s：非 UUID 的 id 必须当不存在", label).isEmpty();
        assertThat(storage.delete("../../etc/passwd")).as("%s：非 UUID 的 id 删除必须返回 false", label).isFalse();

        // 5) delete 的布尔语义：存在 → true；再删 → false
        assertThat(storage.delete(id)).as("%s：删除已存在的对象要返回 true", label).isTrue();
        assertThat(storage.delete(id)).as("%s：删除不存在的对象要返回 false", label).isFalse();

        // 6) 删完就读不到了
        assertThat(storage.read(id)).as("%s：删除之后应当读不到", label).isEmpty();

        // 7) 两次 put 的 id 不同（同一个内容也不能撞 id）
        assertThat(storage.put(CONTENT, CONTENT_TYPE)).as("%s：id 必须每次都不一样", label).isNotEqualTo(id);
    }

    private static void ensureBucket(S3Client client, String bucket) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception exception) {
            // NoSuchBucket 是 S3Exception 的子类，这里只需判 404（不同实现对"桶不存在"给的状态码一致）
            if (exception.statusCode() != 404) {
                throw exception;
            }
            // 桶只有测试自己建：**应用不自动建桶**（配错桶名时自动建会让附件"看起来全消失"）
            client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }
}
