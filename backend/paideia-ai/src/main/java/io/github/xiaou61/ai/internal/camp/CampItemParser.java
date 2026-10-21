package io.github.xiaou61.ai.internal.camp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 题目 JSON 的解析（WORK-029 TASK-008）。
 *
 * <p>只做"把文本变成 {@link ItemValidator.RawItem}"这一件事，**不做结构校验**——校验在
 * {@link ItemValidator}，两件事分开是因为校验规则要被单独穷举测试，而解析只关心形状。
 *
 * <p>三种失败各自抛 {@link CampGenerationException}：
 * <ul>
 *   <li>空内容 / 全空白——**绝不能当成"0 道题"的成功**（DeepSeek 思考 token 吃满
 *       {@code max_tokens} 会静默返回空，那是失败）</li>
 *   <li>不是合法 JSON</li>
 *   <li>JSON 形状不对（没有 items 数组、options 是字符串、answerIndex 不是整数）</li>
 * </ul>
 */
@Component
public class CampItemParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<ItemValidator.RawItem> parse(String rawModelOutput) {
        if (rawModelOutput == null || rawModelOutput.isBlank()) {
            throw CampGenerationException.emptyResponse();
        }
        JsonNode root = readJson(stripFences(rawModelOutput));
        JsonNode items = root.isArray() ? root : root.get("items");
        if (items == null || !items.isArray()) {
            throw CampGenerationException.badShape("模型没有返回 items 数组");
        }
        List<ItemValidator.RawItem> parsed = new ArrayList<>();
        for (JsonNode node : items) {
            parsed.add(toItem(node));
        }
        return parsed;
    }

    /**
     * 模型常把 JSON 包在 ```json 围栏里，尽管提示词说了不要。
     *
     * <p>这里只剥外壳，不做"从半截文本里猜 JSON"这类救援——那会把一次明确的失败变成
     * 一次看起来成功的半截结果。
     */
    static String stripFences(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstBreak = trimmed.indexOf('\n');
        if (firstBreak < 0) {
            return trimmed;
        }
        int closing = trimmed.lastIndexOf("```");
        String body = closing > firstBreak ? trimmed.substring(firstBreak + 1, closing) : trimmed.substring(firstBreak + 1);
        return body.trim();
    }

    private JsonNode readJson(String text) {
        try {
            return mapper.readTree(text);
        } catch (Exception exception) {
            throw CampGenerationException.badShape("模型输出不是合法 JSON");
        }
    }

    private ItemValidator.RawItem toItem(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw CampGenerationException.badShape("items 里有一项不是对象");
        }
        JsonNode optionsNode = node.get("options");
        if (optionsNode == null || !optionsNode.isArray()) {
            throw CampGenerationException.badShape("options 不是数组");
        }
        List<String> options = new ArrayList<>();
        for (JsonNode option : optionsNode) {
            options.add(option.isTextual() ? option.asText() : option.toString());
        }
        JsonNode answer = node.get("answerIndex");
        if (answer == null || !answer.canConvertToInt() || answer.isTextual()) {
            throw CampGenerationException.badShape("answerIndex 不是整数");
        }
        return new ItemValidator.RawItem(
                textOf(node, "topic"),
                textOf(node, "concept"),
                textOf(node, "difficulty"),
                textOf(node, "stem"),
                options,
                answer.asInt(),
                textOf(node, "explanation"));
    }

    private static String textOf(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("");
    }
}
