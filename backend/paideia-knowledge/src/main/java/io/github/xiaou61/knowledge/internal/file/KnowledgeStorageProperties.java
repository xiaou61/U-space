package io.github.xiaou61.knowledge.internal.file;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 附件存储配置。
 *
 * <p>{@code upload-dir} **有默认值**，但默认落在系统临时目录并会在启动时告警。这与
 * WORK-004 设计里"没配置就启动失败"不同，理由是实测的：本地开发机与测试环境都没有这个
 * 环境变量，缺省即失败会让每台机器一上来就起不来；而一个会告警的临时目录至少让人看得见。
 * 生产必须显式配置——告警文案里写明了。
 */
@ConfigurationProperties(prefix = "paideia.knowledge")
public class KnowledgeStorageProperties {

    /** 附件根目录。相对路径按进程工作目录解析。 */
    private String uploadDir = Path.of(System.getProperty("java.io.tmpdir"), "paideia-uploads").toString();

    /** 单文件上限（字节）。默认 10 MB。 */
    private long maxFileSize = 10L * 1024 * 1024;

    /**
     * 附件存储后端：{@code local}（默认，本地目录）或 {@code s3}（S3 兼容对象存储）。
     *
     * <p><b>换实现 = 改这一行 + 重启</b>（WORK-007 用户裁决 5）。刻意不做"运行时不重启热切"：
     * 存储位置会变，热切需要额外的一致性设计，收益不抵风险。
     */
    private String storage = "local";

    /** 对象存储参数。只在 {@code storage=s3} 时被用到。 */
    private final S3 s3 = new S3();

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public S3 getS3() {
        return s3;
    }

    /**
     * 对象存储连接参数。
     *
     * <p><b>凭据只从环境变量来</b>（与数据源口令同一纪律）：仓库里只有占位，
     * 线上值写在服务器上被忽略的配置文件里。
     *
     * <p>{@code pathStyle} 默认 true：自建 S3（RustFS/MinIO/SeaweedFS）一律用 path-style，
     * 虚拟主机风格需要 DNS 通配，自建场景里通常没有。
     */
    public static class S3 {

        /** 例 {@code http://127.0.0.1:9000}。 */
        private String endpoint = "";

        private String bucket = "paideia-attachments";

        private String region = "us-east-1";

        private String accessKey = "";

        private String secretKey = "";

        private boolean pathStyle = true;

        /**
         * 预签名 URL 的有效期：拿到 URL 的人**最晚**能用的时刻。
         *
         * <p>给 7 天是为了配合下面的 {@code redirectCache}：同一个签名在一小时内被反复复用，
         * 而 URL 本身要活过那个窗口。
         */
        private Duration presignTtl = Duration.ofDays(7);

        /**
         * 302 跳转本身允许被浏览器缓存多久。
         *
         * <p>这里有个必须算清的账：**签名 URL 每次都是新的**，而浏览器缓存按完整 URL 索引，
         * 所以"每次请求都换一个签名"等于把缓存命中率打到 0（本项之前靠"id 不变"换来的
         * 是一年的浏览器缓存）。让 302 自己缓存一小时，同一小时内的重复加载就用缓存里那个
         * 跳转，既不回后端也不回对象存储。
         *
         * <p><b>必须小于 {@code presignTtl}</b>，否则浏览器会拿着一个已过期的签名跑。
         */
        private Duration redirectCache = Duration.ofHours(1);

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isPathStyle() {
            return pathStyle;
        }

        public void setPathStyle(boolean pathStyle) {
            this.pathStyle = pathStyle;
        }

        public Duration getPresignTtl() {
            return presignTtl;
        }

        public void setPresignTtl(Duration presignTtl) {
            this.presignTtl = presignTtl;
        }

        public Duration getRedirectCache() {
            return redirectCache;
        }

        public void setRedirectCache(Duration redirectCache) {
            this.redirectCache = redirectCache;
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
