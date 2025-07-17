package com.qianyv.jstartaiagent.app;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.qianyv.jstartaiagent.advisors.MyLoggerAdvisor;
import com.qianyv.jstartaiagent.advisors.ReReadingAdvisor;
import com.qianyv.jstartaiagent.chatMemory.MySQLBasedChatMemory;
import com.qianyv.jstartaiagent.service.ConversationMemoryService;
import com.qianyv.jstartaiagent.service.impl.ConversationMemoryServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {


    private final ChatClient chatClient;

    private final String SYSTEM_PROMPT = "你是「恋爱小助手」AI，专注为用户解决核心恋爱场景中的具体问题，以轻量化、高实用性为原则提供服务。当前处于 MVP 阶段，需优先实现以下核心能力：\n" +
            "核心功能定位\n" +
            "快速判断用户问题所属核心场景：表白 / 暗恋困惑、情侣矛盾处理（吵架、冷战等）、异地恋 / 跨国恋维系、亲密关系沟通技巧、复合挽回决策，对模糊问题主动引导补充关键信息（如：“你们最近一次冲突的具体触发事件是？”）。\n" +
            "基于心理学基础理论（如非暴力沟通、依恋理论）和真实案例数据，提供 3-5 条具体可操作的行动建议，避免冗长理论。示例：用户 “和男友吵架后他冷战三天了，怎么办？” 回复 “① 先冷静 1 天避免情绪对抗；② 用‘我观察到… 我感到… 我需要…’句式开启非暴力沟通；③ 准备一个共同回忆的小物件作为破冰契机。”\n" +
            "开头用共情语句建立信任（如：“能感受到你现在很焦虑，别着急，我们一起分析”），避免机械回复，适当使用表情符号（❤\uFE0F、\uD83E\uDD14、\uD83C\uDF1F）增强亲和力。";

    public LoveApp(ChatModel dashScopeChatModel, ConversationMemoryService conversationMemoryService) {
        ChatMemory chatMemory = new MySQLBasedChatMemory(conversationMemoryService);//基于MySQL的对话记忆
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)//系统提示词
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),//获取记忆
                        new MyLoggerAdvisor()//日志拦截器
                        // new ReReadingAdvisor()//重读拦截器
                )//创建拦截器
                .build();

    }


    /**
     * 会话方法
     *
     * @param message 用户输入的文本
     * @param charId  会话id，后续可以使用这个id来获取会话的上下文记忆，现在可以不使用
     * @return 返回给用户的文本
     */
    public String doChat(String message, String charId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                //这里的advisors是单词发送执行的拦截器，指定了
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)//指定会话房间
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))//指定记忆多少轮对话
                .call()//发送请求
                .chatResponse();//获取返回结果
        String respText = chatResponse.getResult().getOutput().getText();
        log.info(respText);
        return respText;
    }

    public Flux<String> doChatByStream(String message, String charId) {
        Flux<String> content = chatClient.prompt()
                .user(message)
                //这里的advisors是单词发送执行的拦截器，指定了
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)//指定会话房间
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))//指定记忆多少轮对话
                .stream()//使用流式输出
                .content();

        return content;
    }


    //快速定义类属性
    record LoveReport(String title, List<String> suggestion) {
    }

    /**
     * 测试json结构化输出
     */
    public LoveReport doChatTestRePut(String message, String charId) {


        LoveReport loveReport = chatClient.prompt()
                .user(message)
                .system(SYSTEM_PROMPT + "每次输出的报告建议内容的结果，标题{用户名}，内容为建议列表")
                //这里的advisors是单词发送执行的拦截器，指定了
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)//指定会话房间
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))//指定记忆多少轮对话
                .call()//发送请求
                .entity(LoveReport.class);//获取返回结果
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    /**
     * 基于本地RAG知识库问答，且是使用QuestionAnswerAdvisor查询增强
     */
    @Resource
    private VectorStore loveAppVectorStore;

    public String doChatWithRag(String message, String charId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .system(SYSTEM_PROMPT + "每次输出的报告建议内容的结果，标题{用户名}，内容为建议列表")
                //这里的advisors是单词发送执行的拦截器，指定了
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)//指定会话房间
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))//指定记忆多少轮对话
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))//使用问答拦截器
                .call()
                .chatResponse();
        String text = chatResponse.getResult().getOutput().getText();
        log.info("text: {}", text);
        return text;
    }

    /**
     * 基于 阿里云端RAG知识库问答，且是使用RetrievalAugmentationAdvisor查询增强
     */
    @Resource
    private Advisor loveAppRagCloudAdvisor;

    public String doChatWithRagCloud(String message, String charId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .advisors(loveAppRagCloudAdvisor)//基于RetrievalAugmentationAdvisor检索增强
                .call()
                .chatResponse();
        String text = chatResponse.getResult().getOutput().getText();
        log.info("text: {}", text);
        return text;
    }

    @Resource
    private Advisor myRagAdvisor;

    public String doChatWithMyRagAdvisor(String message, String charId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .advisors(myRagAdvisor)//基于RetrievalAugmentationAdvisor检索增强
                .call()
                .chatResponse();
        String text = chatResponse.getResult().getOutput().getText();
        log.info("text: {}", text);
        return text;
    }

    public Flux<String> doChatWithMyRagAdvisorBySSE(String message, String charId) {
        Flux<String> content = chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, charId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .advisors(myRagAdvisor)//基于RetrievalAugmentationAdvisor检索增强
                .stream()
                .content();

        return content;
    }



    /**
     * 使用AI工具
     * 这里使用了ToolCallback[]数组来获取所有的工具
     */
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



}
