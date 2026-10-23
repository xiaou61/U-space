package io.github.xiaou61;

import io.github.xiaou61.knowledge.AttachmentReader;
import io.github.xiaou61.sandbox.DatasetStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据集存储的桥接（WORK-035 T-09）。
 *
 * <p><b>为什么桥接放在装配根</b>：`DatasetStorage` 是 sandbox 的端口，实际字节由
 * knowledge 持有（WORK-007 的对象存储）。sandbox 直接依赖 knowledge 会反转模块方向，
 * 而引 `knowledge.internal.file.FileStorage` 会被 `ModularityTest` 拦下——所以 knowledge
 * 侧开了 {@link AttachmentReader} 这个公开契约，这里把它接到 sandbox 的端口上。
 *
 * <p>这层桥接是**唯一的接缝**：两个模块都不认识对方，装配根认识两者。
 */
@Configuration
public class DatasetStorageConfiguration {

    @Bean
    DatasetStorage datasetStorage(AttachmentReader attachments) {
        return new DatasetStorage() {
            @Override
            public java.util.Optional<byte[]> read(String storageId) {
                return attachments.read(storageId);
            }

            @Override
            public String put(byte[] content, String contentType) {
                return attachments.put(content, contentType);
            }
        };
    }
}
