package com.xiaou.ai.controller;


import com.xiaou.ai.service.SmartGeneratorService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 流式响应控制器
 * 提供Server-Sent Events (SSE) 流式聊天功能
 *
 * @author Spring AI Demo
 */
@RestController
@Slf4j
@RequestMapping("/ai/deepseek/stream")
public class StreamController {


    @Resource
    private SmartGeneratorService smartGeneratorService;

    /**
     * 流式聊天API
     * 使用Server-Sent Events (SSE) 实现实时流式响应
     *
     * @param message 用户消息
     * @return 流式响应
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        log.info("收到流式聊天请求：{}", message);

        return smartGeneratorService.streamChat(message)
                .filter(chunk -> chunk != null && !chunk.trim().isEmpty()) // 过滤空内容
                .doOnNext(chunk -> log.debug("原始数据块: '{}'", chunk))
                .map(chunk -> chunk.trim()) // 只清理空白字符
                .filter(chunk -> !chunk.isEmpty()) // 再次过滤空内容
                .concatWith(Flux.just("[DONE]"))
                .doOnSubscribe(subscription -> log.info("开始流式响应"))
                .doOnComplete(() -> log.info("流式响应完成"))
                .doOnError(error -> log.error("流式响应出错", error))
                .onErrorReturn("[ERROR] 流式响应出现错误");
    }

    /**
     * 流式聊天API（JSON格式）
     * 返回JSON格式的流式数据
     *
     * @param message 用户消息
     * @return JSON格式的流式响应
     */
    @GetMapping(value = "/chat-json", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Map<String, Object>> streamChatJson(@RequestParam String message) {
        log.info("收到JSON流式聊天请求：{}", message);

        // 创建完成响应
        Map<String, Object> doneResponse = new HashMap<>();
        doneResponse.put("type", "done");
        doneResponse.put("content", "");
        doneResponse.put("timestamp", System.currentTimeMillis());

        // 创建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "error");
        errorResponse.put("content", "流式响应出现错误");
        errorResponse.put("timestamp", System.currentTimeMillis());

        return smartGeneratorService.streamChat(message)
                .map(chunk -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "chunk");
                    response.put("content", chunk);
                    response.put("timestamp", System.currentTimeMillis());
                    return response;
                })
                .concatWith(Flux.just(doneResponse))
                .doOnSubscribe(subscription -> log.info("开始JSON流式响应"))
                .doOnComplete(() -> log.info("JSON流式响应完成"))
                .doOnError(error -> log.error("JSON流式响应出错", error))
                .onErrorReturn(errorResponse);
    }

    /**
     * 模拟打字机效果的流式响应
     *
     * @param message 用户消息
     * @return 带延迟的流式响应
     */
    @GetMapping(value = "/typewriter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> typewriterChat(@RequestParam String message) {
        log.info("收到打字机效果聊天请求：{}", message);

        return smartGeneratorService.streamChat(message)
                .delayElements(Duration.ofMillis(50)) // 添加50ms延迟模拟打字机效果
                .map(chunk -> "data: " + chunk + "\n\n")
                .concatWith(Flux.just("data: [DONE]\n\n"))
                .doOnSubscribe(subscription -> log.info("开始打字机效果流式响应"))
                .doOnComplete(() -> log.info("打字机效果流式响应完成"))
                .doOnError(error -> log.error("打字机效果流式响应出错", error))
                .onErrorReturn("data: [ERROR] 流式响应出现错误\n\n");
    }

    //带有记忆的流式聊天
    @GetMapping(value = "/chat-memory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatMemory(@RequestParam String message) {
        Long userId = LoginHelper.getCurrentAppUserId();
        return smartGeneratorService.streamChatWithMemory(userId.toString(), message)
                .concatWith(Flux.just("[DONE]"));
    }


}