package com.xiaou.ai.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class SmartGeneratorService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ReactiveRedisOperations<String, String> redisOps;

    private static final int MAX_HISTORY = 10; // 保留最近10条对话

    /**
     * 流式聊天
     *
     * @param message 用户消息
     * @return 流式响应
     */
    public Flux<String> streamChat(String message) {
        log.info("开始流式聊天，消息：{}", message);

        try {
            String systemPrompt = """
                    你是一位友好、有帮助的AI助手。
                    请以自然、亲切的方式与用户对话，用中文回复。
                    """;

            PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "\n\n用户：{content}");
            Prompt prompt = promptTemplate.create(Map.of("content", message));

            DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                    .temperature(0.9)
                    .maxTokens(800)
                    .build();

            return chatModel.stream(new Prompt(prompt.getInstructions(), options))
                    .map(response -> response.getResult().getOutput().getText())
                    .doOnNext(chunk -> log.debug("流式响应块：{}", chunk))
                    .doOnComplete(() -> log.info("流式聊天完成"))
                    .doOnError(error -> log.error("流式聊天失败", error));

        } catch (Exception e) {
            log.error("流式聊天启动失败", e);
            return Flux.error(e);
        }
    }


    /**
     * 带记忆的流式聊天
     *
     * @param sessionId 会话ID，唯一标识用户会话
     * @param message   用户消息
     * @return 流式响应
     */
    public Flux<String> streamChatWithMemory(String sessionId, String message) {
        ReactiveListOperations<String, String> listOps = redisOps.opsForList();
        String redisKey = "chat:history:" + sessionId;

        return listOps.range(redisKey, 0, MAX_HISTORY - 1)
                .collectList()
                .flatMapMany(historyList -> {
                    // 拼接系统提示和上下文历史
                    StringBuilder sb = new StringBuilder();
                    sb.append("你是一位友好、有帮助的AI助手。\n");
                    sb.append("请以自然、亲切的方式与用户对话，用中文回复。\n\n");
                    for (String past : historyList) {
                        sb.append("用户：").append(past).append("\n");
                    }
                    sb.append("用户：").append(message);

                    PromptTemplate promptTemplate = new PromptTemplate(sb.toString());
                    Prompt prompt = promptTemplate.create();

                    DeepSeekChatOptions options = DeepSeekChatOptions.builder()
                            .temperature(0.9)
                            .maxTokens(800)
                            .build();

                    // 调用模型
                    Flux<String> responseFlux = chatModel.stream(new Prompt(prompt.getInstructions(), options))
                            .map(resp -> resp.getResult().getOutput().getText())
                            .doOnNext(chunk -> log.debug("流式响应块：{}", chunk))
                            .doOnComplete(() -> log.info("流式聊天完成"))
                            .doOnError(error -> log.error("流式聊天失败", error));

                    // 响应流完成后，保存用户本次输入到 Redis 头部（最新）
                    return responseFlux.concatWith(Mono.defer(() -> {
                        // 先push新的消息，再修剪list保持长度
                        return listOps.leftPush(redisKey, message)
                                .flatMap(len -> listOps.trim(redisKey, 0, MAX_HISTORY - 1))
                                .then(Mono.empty());
                    }));
                })
                .onErrorResume(e -> {
                    log.error("带记忆流式聊天失败", e);
                    return Flux.just("[ERROR] 聊天出错");
                });
    }

}
