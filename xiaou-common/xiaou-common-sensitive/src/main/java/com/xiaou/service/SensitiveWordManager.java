package com.xiaou.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.core.SensitiveTrie;
import com.xiaou.model.MatchResult;
import com.xiaou.model.TrieNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import com.xiaou.redis.utils.RedisUtils;

/**
 * 敏感词管理器（无锁版）
 * 适用于写操作很少且并发不高的场景
 */
@Component
public class SensitiveWordManager {

    private static final String REDIS_KEY = "sensitive:trie:json";

    private SensitiveTrie trie;

    @Resource
    private SensitiveWordLoader loader;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 内存缓存的敏感词Map，key为词，value为敏感级别
     */
    private Map<String, Integer> wordMap = new HashMap<>();

    @PostConstruct
    public void init() {
        reload();
    }

    /**
     * 从Redis加载敏感词Trie，并更新内存缓存
     */
    public void reload() {
        TrieNode root = loader.load();
        this.trie = new SensitiveTrie(root);
        this.wordMap = extractWordsFromTrie(root);
    }

    /**
     * 过滤输入文本
     */
    public SensitiveFilterResult filter(String content) {
        MatchResult result = trie.matchWithLevel(content);

        if (result.hasLevel(1)) {
            return SensitiveFilterResult.blocked(result.getWordsByLevel(1));
        }

        String replaced = trie.replace(content, result.getWordsByLevel(2), '*');
        return SensitiveFilterResult.allowed(replaced, result.getWordsByLevel(2));
    }

    /**
     * 获取所有敏感词快照
     */
    public Map<String, Integer> listAll() {
        return new HashMap<>(wordMap);
    }

    /**
     * 新增或更新敏感词
     */
    public void addWord(String word, int level) {
        wordMap.put(word, level);
        rebuildTrieAndSave();
    }

    /**
     * 删除敏感词
     */
    public void deleteWord(String word) {
        wordMap.remove(word);
        rebuildTrieAndSave();
    }

    /**
     * 重建Trie并同步到Redis
     */
    private void rebuildTrieAndSave() {
        TrieNode root = buildTrieFromWordMap(wordMap);
        try {
            String json = mapper.writeValueAsString(root);
            RedisUtils.getClient()
                    .getBucket(REDIS_KEY, org.redisson.client.codec.StringCodec.INSTANCE)
                    .set(json);  // ✅ 确保存的是字符串
        } catch (Exception e) {
            throw new RuntimeException("序列化敏感词 Trie 失败", e);
        }
        this.trie = new SensitiveTrie(root);
    }


    private TrieNode buildTrieFromWordMap(Map<String, Integer> words) {
        TrieNode root = new TrieNode();
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            String word = entry.getKey();
            int level = entry.getValue();
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
            }
            node.setEnd(true);
            node.setLevel(level);
        }
        return root;
    }

    private Map<String, Integer> extractWordsFromTrie(TrieNode root) {
        Map<String, Integer> map = new HashMap<>();
        collectWords(root, new StringBuilder(), map);
        return map;
    }

    private void collectWords(TrieNode node, StringBuilder path, Map<String, Integer> map) {
        if (node.isEnd()) {
            map.put(path.toString(), node.getLevel());
        }
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            path.append(entry.getKey());
            collectWords(entry.getValue(), path, map);
            path.deleteCharAt(path.length() - 1);
        }
    }
}
