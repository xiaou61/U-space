package com.xiaou.ai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.ai.agent.YuManus;
import com.xiaou.ai.app.CampusApp;
import com.xiaou.ai.entity.ChatMessage;
import com.xiaou.ai.mapper.ChatMessageMapper;
import com.xiaou.common.domain.R;
import com.xiaou.ratelimiter.annotation.RateLimiter;
import com.xiaou.ratelimiter.enums.LimitType;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private CampusApp campusApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;
    @Autowired
    private LoginHelper loginHelper;

    @Resource
    private ChatMessageMapper chatMessageMapper;
    /**
     * 同步调用 AI 校园应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/campus_app/chat/sync")
    public String doChatWithCampusAppSync(String message, String chatId) {
        return campusApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 校园应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/campus_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithCampusAppSSE(String message, String chatId) {
        return campusApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 校园应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/campus_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithCampusAppServerSentEvent(String message, String chatId) {
        return campusApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 AI 校园应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/campus_app/chat/sse_emitter")
    public SseEmitter doChatWithCampusAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        campusApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        YuManus yuManus = new YuManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }

    /**
     * 同步调用 AI 校园应用（启用工具调用）
     */
    @GetMapping("/campus_app/chat/tools")
    public String doChatWithCampusAppTools(String message, String chatId) {
        return campusApp.doChatWithTools(message, chatId);
    }

    /**
     * SSE 调用 AI 校园应用（启用工具调用）
     */
    @GetMapping(value = "/campus_app/chat/tools_sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithCampusAppToolsSse(String message, String chatId) {
        return campusApp.doChatWithToolsByStream(message, chatId);
    }

    /**
     * 通用流式对话接口，可通过参数选择是否启用 RAG、工具、本地或 MCP。
     * 示例：/ai/campus_app/chat/stream?message=...&chatId=123&tools=true&rag=true&mcp=false
     */
    @RateLimiter(limitType = LimitType.IP, count = 10)
    @GetMapping(value = "/campus_app/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatStreamOptions(String message,
            @RequestParam(defaultValue = "false") boolean tools,
            @RequestParam(defaultValue = "false") boolean rag) {
        return campusApp.doChatByOptionsStream(message, loginHelper.getCurrentAppUserId(), tools, rag)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .data("[DONE]")
                        .event("end")
                        .build()));
    }

    /**
     * 查看自己的对话信息
     */
    @GetMapping("/chat/list")
    public R<List<ChatMessage>> list() {
        List<ChatMessage> list = chatMessageMapper.selectList(new QueryWrapper<ChatMessage>().eq("user_id", loginHelper.getCurrentAppUserId()));
        return R.ok(list);
    }

    /**
     * 查看所有人的对话信息
     */
    @GetMapping("/chat/list/all")
    @SaCheckRole(value = RoleConstant.ADMIN)
    public R<List<ChatMessage>> listAll() {
        return R.ok(chatMessageMapper.selectList(null));
    }
}
