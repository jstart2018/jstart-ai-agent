package com.qianyv.jstartaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    //todo 暂用阿里的灵积EmbeddingModel，有额度的可以开
    //@Bean
    VectorStore dashScopeVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        List<Document> documents = loveAppDocumentLoader.loadDocuments();

        SimpleVectorStore vectorStore =
                SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        vectorStore.add(documents);
        return vectorStore;
    }

    @Bean
    VectorStore siliconflowVectorStore(EmbeddingModel siliconflowEmbeddingModel) {
        List<Document> documents = loveAppDocumentLoader.loadDocuments();

        SimpleVectorStore vectorStore =
                SimpleVectorStore.builder(siliconflowEmbeddingModel).build();
        vectorStore.add(documents);
        return vectorStore;
    }

}
