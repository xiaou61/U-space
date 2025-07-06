package com.xiaou.ai.app;

import com.xiaou.ai.advisor.MyLoggerAdvisor;
import com.xiaou.ai.entity.ChatMessage;
import com.xiaou.ai.rag.QueryRewriter;
import com.xiaou.mq.utils.RabbitMQUtils;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import com.xiaou.ai.chatmemory.RedisChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 智慧校园助手应用。
 * <p>
 * 具备多轮对话、向量检索增强 (RAG)、工具调用、结构化输出等能力。
 */
@Component
@Slf4j
public class CampusApp {

    private final ChatClient chatClient;
    @Resource
    private LoginHelper loginHelper;

    private static final String SYSTEM_PROMPT = "你是一名智慧校园领域的专家级 AI 助手。开场向用户说明你可处理校园学习、生活及行政相关问题。" +
            "围绕学业指导、课程检索、校园活动、宿舍生活、社团兴趣、图书馆资源、教务事务等维度提问：" +
            "如果用户关心学业，可询问专业、课程难点、学习方法；" +
            "如果用户关心校园生活，可询问住宿、饮食、社团活动；" +
            "如果用户关心行政或办事流程，可询问具体流程与遇到的阻碍。" +
            "引导用户描述问题背景、已尝试方案及期望结果，以便给出个性化解决建议。";

    @Resource
    private RabbitMQUtils rabbitMQUtils;

    public CampusApp(ChatModel dashscopeChatModel, RedisChatMemoryRepository redisRepo) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisRepo)
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor
                        new MyLoggerAdvisor()
                        // ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话（SSE 流式）
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    // 结构化输出示例
    record CampusReport(String title, List<String> suggestions) {
    }

    public CampusReport doChatWithReport(String message, String chatId) {
        CampusReport report = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成校园助手报告，标题为{用户名}的校园助手报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(CampusReport.class);
        log.info("CampusReport: {}", report);
        return report;
    }

    // === RAG 知识库 ===
    @Resource
    private VectorStore campusVectorStore;

    @Resource
    private Advisor campusRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(campusVectorStore))
                // .advisors(campusRagCloudAdvisor)
                // .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // === 工具调用 ===
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 对话（启用工具调用，SSE 流式返回）
     */
    public Flux<String> doChatWithToolsByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .stream()
                .content();
    }

    public Flux<String> doChatByOptionsStream(String message, String chatId,
                                              boolean enableTools,
                                              boolean enableRag) {


        // 构造并初始化 ChatMessage 对象
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserId(chatId);
        chatMessage.setUserContent(message);
        //非0则true
        chatMessage.setEnableRag(enableRag);
        chatMessage.setEnableTools(enableTools);

        // 构建 Prompt
        var promptSpec = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor());

        if (enableRag) {
            promptSpec.advisors(new QuestionAnswerAdvisor(campusVectorStore));
        }

        if (enableTools) {
            promptSpec.toolCallbacks(allTools);
        }

        // 接收 AI 回复（流式），拼接内容
        StringBuilder aiContent = new StringBuilder();

        return promptSpec.stream().content()
                .doOnNext(aiContent::append)
                .doOnComplete(() -> {
                    // 设置 AI 回复内容
                    chatMessage.setAiContent(aiContent.toString());

                    //调用mq
                    rabbitMQUtils.sendAichatMessage(chatMessage);
                });
    }

}