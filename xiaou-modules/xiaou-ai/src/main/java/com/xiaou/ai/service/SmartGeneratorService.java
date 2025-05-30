package com.xiaou.ai.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@Slf4j
public class SmartGeneratorService {

    @Resource
    private ChatModel chatModel;

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

}
