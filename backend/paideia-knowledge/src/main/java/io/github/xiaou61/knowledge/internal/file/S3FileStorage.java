package io.github.xiaou61.knowledge.internal.file;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * S3 兼容对象存储实现（WORK-007）。
 *
 * <p><b>与本地实现逐条对齐</b>，这是它唯一可接受的行为标准——两种实现要能被同一批用例跑过，
 * 否则"换实现"就是空的：
 * <ul>
 *   <li>{@code put}：key 就是新生成的 UUID（<b>不是</b>自增、不是原名），{@code contentType}
 *       成为对象的 Content-Type 元数据——端口注释里当初预留的那句"实现可以拿它去做对象元数据"
 *       指的正是这里。</li>
 *   <li>{@code read}：对象不存在 → {@code Optional.empty()}。**"文件没了"仍然是可预期状态，
 *       不是异常**（本地实现读不到也是返回空）。</li>
 *   <li>{@code delete}：**先 head 再删**。S3 的 DeleteObject 是幂等的、对象不存在也回 204，
 *       所以直接删无法给出"到底有没有删掉"这个布尔值；而端口的契约要求不存在时返回 false。</li>
 *   <li>{@code presignedGetUrl}：本地实现没有这个能力（返回空），这里给出带签名、有时效的直读 URL。</li>
 * </ul>
 *
 * <p><b>桶不自动创建</b>：配置里写错桶名时，自动建桶会让"附件看起来全都消失了"（其实进了另一个桶），
 * 而那个错误极难排查。宁可第一次上传就报"NoSuchBucket"，也不替运维做基础设施的事。
 */
class S3FileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(S3FileStorage.class);

    private final S3Client client;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration presignTtl;

    S3FileStorage(S3Client client, S3Presigner presigner, String bucket, Duration presignTtl) {
        this.client = client;
        this.presigner = presigner;
        this.bucket = bucket;
        this.presignTtl = presignTtl;
    }

    @Override
    public String put(byte[] content, String contentType) {
        String id = UUID.randomUUID().toString();
        try {
            client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(id).contentType(contentType).build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception exception) {
            // 不把桶名之外的东西回给调用方；日志里有桶名与状态码足够定位
            log.error("附件写入对象存储失败：桶={} 状态={}", bucket, exception.statusCode(), exception);
            throw new BizException(ErrorCode.INTERNAL, "附件写入失败");
        }
        return id;
    }

    @Override
    public Optional<byte[]> read(String id) {
        if (!FileIds.isUuid(id)) {
            return Optional.empty();
        }
        try (ResponseInputStream<GetObjectResponse> stream =
                client.getObject(GetObjectRequest.builder().bucket(bucket).key(id).build())) {
            return Optional.of(readAll(stream));
        } catch (NoSuchKeyException exception) {
            return Optional.empty();
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                // 有些实现（含 RustFS 的某些路径）对不存在的对象给的是通用 404 而不是 NoSuchKey
                return Optional.empty();
            }
            log.error("读取附件失败：桶={} 状态={}", bucket, exception.statusCode(), exception);
            throw new BizException(ErrorCode.INTERNAL, "附件读取失败");
        } catch (IOException exception) {
            log.error("读取附件流失败：桶={}", bucket, exception);
            throw new BizException(ErrorCode.INTERNAL, "附件读取失败");
        }
    }

    @Override
    public boolean delete(String id) {
        if (!FileIds.isUuid(id)) {
            return false;
        }
        try {
            client.headObject(HeadObjectRequest.builder().bucket(bucket).key(id).build());
        } catch (NoSuchKeyException exception) {
            return false;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }
            log.error("删除前确认附件失败：桶={} 状态={}", bucket, exception.statusCode(), exception);
            throw new BizException(ErrorCode.INTERNAL, "附件删除失败");
        }
        try {
            client.deleteObject(builder -> builder.bucket(bucket).key(id));
            return true;
        } catch (S3Exception exception) {
            log.error("删除附件失败：桶={} 状态={}", bucket, exception.statusCode(), exception);
            throw new BizException(ErrorCode.INTERNAL, "附件删除失败");
        }
    }

    @Override
    public Optional<String> presignedGetUrl(String id, Duration ttl) {
        if (!FileIds.isUuid(id)) {
            return Optional.empty();
        }
        // 调用方给的 ttl 优先；给零或负值时退回配置里的默认有效期（不给出"立刻失效"的 URL）
        Duration effective = ttl == null || ttl.isZero() || ttl.isNegative() ? presignTtl : ttl;
        String url = presigner
                .presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(effective)
                        .getObjectRequest(builder -> builder.bucket(bucket).key(id))
                        .build())
                .url()
                .toString();
        return Optional.of(url);
    }

    /** 附件上限是 10MB，整块读进来与本地实现的内存行为一致（端口返回的就是 byte[]）。 */
    private static byte[] readAll(InputStream stream) throws IOException {
        return stream.readAllBytes();
    }
}
