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

    // todo 暂用阿里的灵积EmbeddingModel，有额度的可以开
/*    @Resource
    private VectorStore loveAppVectorStore;*/


    @Resource
    private VectorStore siliconflowVectorStore;

    @Value("${spring.ai.dashscope.api-key}")
    private String api_key;

    // todo 暂不使用这个bean，需要再开启@Bean
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
        String promptTemplate = """
			Context information is below.

			---------------------
			{context}
			---------------------

			Given the context information and no prior knowledge, answer the query.

			Follow these rules:

			1. If the answer does not match the context, but questions related to family, friendship, or love should be answered based on one's own knowledge, otherwise refuse to answer.
			2. Avoid statements like "Based on the context..." or "The provided information...".

			Query: {query}

			Answer:
			""";

        //定义元信息过滤表达式
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                .eq("status","单身").build();

        VectorStoreDocumentRetriever vsdr = VectorStoreDocumentRetriever.builder()
                .vectorStore(siliconflowVectorStore) //使用本地向量存储
                .similarityThreshold(0.45d) //用硅基流动的EmbeddingModel要设置大一点，否则很容易检索出不相干知识库
//                .filterExpression(filterExpression)//接收过滤表达式（对元信息过滤）
                .topK(3)
                .build();

        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(vsdr)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .promptTemplate(new PromptTemplate(promptTemplate))
                        .allowEmptyContext(true)
                        .emptyContextPromptTemplate(new PromptTemplate("没有检索到知识库，直接使用你自己的知识回答情感问题，不能回答情感问题以外的问题"))
                        .build())
                .build();
    }

}
