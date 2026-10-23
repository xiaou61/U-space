package io.github.xiaou61;

import io.github.xiaou61.knowledge.internal.file.FileStorage;
import io.github.xiaou61.sandbox.internal.dataset.DatasetStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据集存储的桥接（WORK-035 T-08）。
 *
 * <p><b>为什么桥接放在装配根</b>：`DatasetStorage` 是 sandbox 的端口，实际字节由 knowledge
 * 的 `FileStorage` 持有（WORK-007 的对象存储）。让 sandbox 直接依赖 knowledge 会反转既有的
 * 模块方向，并跨过 `knowledge.internal` 这个不对外导出的包——`ModularityTest` 会拦下。
 * 装配根同时认识两者，正好做这一层。
 *
 * <p>**引用 internal 包是否合规**：`FileStorageConfiguration` 已经在装配根之外被 app 依赖
 * （`paideia-app` 的 pom 同时声明 knowledge 与 sandbox）。这里跨 internal 读取与既有
 * `KnowledgeStorageProperties` 的用法一致——**唯一的接缝就在这里**，别在别处再引一次。
 */
@Configuration
public class DatasetStorageConfiguration {

    @Bean
    DatasetStorage datasetStorage(FileStorage storage) {
        return storage::read;
    }
}
