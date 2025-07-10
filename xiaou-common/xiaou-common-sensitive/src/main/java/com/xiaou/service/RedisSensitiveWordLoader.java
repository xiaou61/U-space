package com.xiaou.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.model.TrieNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;

@Component
public class RedisSensitiveWordLoader implements SensitiveWordLoader {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY = "sensitive:trie:json";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public TrieNode load() {
        String json = redisTemplate.opsForValue().get(REDIS_KEY);
        if (json == null || json.isEmpty()) {
            // Redis没有数据，初始化默认敏感词Trie
            TrieNode root = buildDefaultTrie();
            try {
                String serialized = mapper.writeValueAsString(root);
                redisTemplate.opsForValue().set(REDIS_KEY, serialized);
            } catch (Exception e) {
                throw new RuntimeException("序列化默认敏感词Trie失败", e);
            }
            return root;
        }

        try {
            return mapper.readValue(json, TrieNode.class);
        } catch (Exception e) {
            throw new RuntimeException("敏感词 Trie 加载失败", e);
        }
    }

    /**
     * 构建默认敏感词Trie，包含“傻逼”（level=2）
     */
    private TrieNode buildDefaultTrie() {
        TrieNode root = new TrieNode();
        insertWord(root, "傻逼", 2);
        return root;
    }

    /**
     * 往Trie树插入一个词和对应的等级
     */
    private void insertWord(TrieNode root, String word, int level) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            Map<Character, TrieNode> children = node.getChildren();
            if (!children.containsKey(c)) {
                children.put(c, new TrieNode());
            }
            node = children.get(c);
        }
        node.setEnd(true);
        node.setLevel(level);
    }
}
