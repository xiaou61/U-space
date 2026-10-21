package io.github.xiaou61.ai.internal.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.ToolSchema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把一轮 LLM 交互整理成可入库的 JSON（WORK-030 阶段 B）。
 *
 * <p><b>只挑安全字段，不做整对象序列化</b>——这是本类存在的唯一理由。
 * `GenerateOptions` 上有 `getApiKey()`；`Model` 的实现里可能还有 baseUrl。
 * 直接 `writeValueAsString(options)` 会把凭据写进数据库，而且写进的是**一张管理员能读的表**。
 * 所以这里白名单式地逐个取字段，新增字段必须显式加进来。
 *
 * <p>**存"实际发送"而不是"打算发送"**（REQ-061）：入参拿的就是 `ModelCallInput` 里那份
 * messages——序列化层做的裁剪、注入的 system 提示都已经在它里面了。
 */
final class PayloadJson {

    private static final ObjectMapper JSON = new ObjectMapper();

    private PayloadJson() {
    }

    /**
     * 请求体。
     *
     * @param messages 实际发出去的 messages（已含 system/历史/本轮输入）
     * @param tools    这一轮发给模型的工具清单
     * @param options  采样参数；**只取数值项**，凭据与地址一律不取
     */
    static String request(List<Msg> messages, List<ToolSchema> tools, GenerateOptions options) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", messages == null ? List.of() : messages.stream().map(PayloadJson::message).toList());
        body.put("tools", tools == null ? List.of() : tools.stream().map(PayloadJson::tool).toList());
        if (options != null) {
            Map<String, Object> params = new LinkedHashMap<>();
            putIfPresent(params, "model", options.getModelName());
            putIfPresent(params, "temperature", options.getTemperature());
            putIfPresent(params, "topP", options.getTopP());
            putIfPresent(params, "maxTokens", options.getMaxTokens());
            putIfPresent(params, "stream", options.getStream());
            body.put("params", params);
        }
        return write(body);
    }

    /**
     * 响应体。
     *
     * @param text      这一轮模型吐出的正文
     * @param thinking  思考内容（不进历史，但排查时要看得到）
     * @param toolCalls 这一轮发出的工具调用
     */
    static String response(String text, String thinking, List<ToolUseBlock> toolCalls) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text == null ? "" : text);
        if (thinking != null && !thinking.isEmpty()) {
            body.put("thinking", thinking);
        }
        if (toolCalls != null && !toolCalls.isEmpty()) {
            body.put("toolCalls", toolCalls.stream().map(call -> {
                Map<String, Object> one = new LinkedHashMap<>();
                one.put("id", call.getId());
                one.put("name", call.getName());
                one.put("input", call.getInput());
                return one;
            }).toList());
        }
        return write(body);
    }

    private static Map<String, Object> message(Msg msg) {
        Map<String, Object> one = new LinkedHashMap<>();
        one.put("role", msg.getRole() == null ? null : msg.getRole().name());
        List<String> parts = new ArrayList<>();
        List<ToolUseBlock> toolCalls = new ArrayList<>();
        if (msg.getContent() != null) {
            for (ContentBlock block : msg.getContent()) {
                if (block instanceof TextBlock text) {
                    parts.add(text.getText() == null ? "" : text.getText());
                } else if (block instanceof ThinkingBlock thinking) {
                    // 思考单独标记：混进正文会让"他为什么这么答"读不出来
                    parts.add("[thinking] " + (thinking.getThinking() == null ? "" : thinking.getThinking()));
                } else if (block instanceof ToolUseBlock call) {
                    toolCalls.add(call);
                }
            }
        }
        one.put("text", String.join("\n", parts));
        if (!toolCalls.isEmpty()) {
            one.put("toolCalls", toolCalls.stream().map(call -> {
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("id", call.getId());
                detail.put("name", call.getName());
                detail.put("input", call.getInput());
                return detail;
            }).toList());
        }
        return one;
    }

    private static Map<String, Object> tool(ToolSchema schema) {
        Map<String, Object> one = new LinkedHashMap<>();
        one.put("name", schema.getName());
        one.put("description", schema.getDescription());
        one.put("parameters", schema.getParameters());
        return one;
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    /** 序列化失败返回 null：一个字段写不出来不该让整条记录丢失。 */
    private static String write(Map<String, Object> body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (Exception failure) {
            return null;
        }
    }
}
