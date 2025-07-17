package com.qianyv.jstartaiagent.rag;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentCloudReader;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    @Resource
    private VectorStore loveAppVectorStore;

    @Value("${spring.ai.dashscope.api-key}")
    private String api_key;

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        DashScopeApi dashScopeApi = new DashScopeApi(api_key);

        String KNOWLEDGE_INDEX = "恋爱大师"; //阿里云上面知识库的名称

        DashScopeDocumentRetriever dashScopeDocumentRetriever =
                new DashScopeDocumentRetriever(dashScopeApi,//API-KEY
                    DashScopeDocumentRetrieverOptions
                            .builder()
                            .withIndexName(KNOWLEDGE_INDEX)//指定使用的知识库
                            .build());
        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(dashScopeDocumentRetriever)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true).build())//允许空上下文
                .build();
    }

    @Bean
    public Advisor myRagAdvisor() {

        //定义元信息过滤表达式
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                .eq("status","单身").build();

        VectorStoreDocumentRetriever vsdr = VectorStoreDocumentRetriever.builder()
                .vectorStore(loveAppVectorStore) //使用本地向量存储
                .similarityThreshold(0.24)
                .filterExpression(filterExpression)//接收过滤表达式（对元信息过滤）
                .topK(5)
                .build();

        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(vsdr)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .emptyContextPromptTemplate(new PromptTemplate("检索到空知识库了，不再使用知识库知识，直接使用你大模型的知识回答"))
                        .build())
                .build();
    }

}
