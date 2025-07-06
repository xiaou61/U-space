package com.xiaou.ai.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 智慧校园向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
@Configuration
public class CampusVectorStoreConfig {

    @Resource
    private CampusDocumentLoader campusDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore campusVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 仅创建空的向量存储，文档加载移至异步初始化器
        return SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
    }
}