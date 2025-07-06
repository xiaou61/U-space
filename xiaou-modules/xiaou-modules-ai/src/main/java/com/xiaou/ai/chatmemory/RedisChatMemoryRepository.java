package com.xiaou.ai.chatmemory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisChatMemoryRepository implements ChatMemoryRepository {


    private static final String KEY_PREFIX = "chat:mem:"; // chat:mem:{conversationId}
    private static final Duration DEFAULT_TTL = Duration.ofDays(7);

    private final ObjectMapper objectMapper;

    private String key(String conversationId) {

        // 原来: chat:mem:{conversationId}
        return KEY_PREFIX + ":" + conversationId;

    }

    @Override
    public List<String> findConversationIds() {
        Set<String> keys = (Set<String>) RedisUtils.keys(KEY_PREFIX + "*");
        return keys.stream()
                .map(k -> k.substring(KEY_PREFIX.length()))
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public List<Message> findByConversationId(String conversationId) {
        List<String> jsonList = RedisUtils.getCacheList(key(conversationId));
        return jsonList.stream()
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, Message.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public void saveAll(String conversationId, List<Message> messages) {
        List<String> jsonList = messages.stream()
                .map(m -> {
                    try {
                        return objectMapper.writeValueAsString(m);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(s -> s != null)
                .collect(Collectors.toList());
        // 覆盖式写入
        RedisUtils.setCacheList(key(conversationId), jsonList);
        RedisUtils.expire(key(conversationId), DEFAULT_TTL);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        RedisUtils.deleteObject(key(conversationId));
    }
}