package com.xiaou.ai.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 在应用启动完成后异步加载校园文档并写入向量存储，避免阻塞 Spring Boot 启动过程。
 */
@EnableAsync(proxyTargetClass = true)
@Configuration
@Slf4j
public class CampusVectorStoreInitializer {

    @Component
    static class Loader implements ApplicationRunner {

        @Resource
        private VectorStore campusVectorStore;

        @Resource
        private CampusDocumentLoader campusDocumentLoader;

        @Resource
        private MyKeywordEnricher myKeywordEnricher;

        @Async
        @Override
        public void run(ApplicationArguments args) {
            try {
                log.info("开始异步加载校园文档到向量存储……");
                List<Document> docs = campusDocumentLoader.loadMarkdowns();
                List<Document> enriched = myKeywordEnricher.enrichDocuments(docs);
                campusVectorStore.add(enriched);
                log.info("向量文档加载完成，总计 {} 条", enriched.size());
            } catch (Exception e) {
                log.error("向量文档加载失败", e);
            }
        }
    }
}