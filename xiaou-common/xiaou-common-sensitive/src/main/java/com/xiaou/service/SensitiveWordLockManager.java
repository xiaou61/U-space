package com.xiaou.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.core.SensitiveTrie;
import com.xiaou.model.MatchResult;
import com.xiaou.model.TrieNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.xiaou.redis.utils.RedisUtils;

/**
 * 敏感词管理器，支持加载敏感词Trie、过滤文本、(有锁版本)
 * 以及敏感词的增删查改功能，所有敏感词存储于Redis，
 * 并同步维护内存中的Trie结构以提高匹配效率。
 */
@Component
public class SensitiveWordLockManager {

    /**
     * Redis中存储敏感词Trie JSON的key
     */
    private static final String REDIS_KEY = "sensitive:trie:json";

    /**
     * 内存中的敏感词Trie结构，负责高效匹配
     */
    private SensitiveTrie trie;

    /**
     * 敏感词加载器（负责从Redis加载Trie）
     */
    @Resource
    private SensitiveWordLoader loader;

    /**
     * Jackson对象，负责JSON序列化和反序列化
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 读写锁，保证多线程并发访问的安全性
     */
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * 内存缓存的敏感词Map，key为词，value为敏感级别
     * 方便增删改查操作
     */
    private Map<String, Integer> wordMap = new HashMap<>();

    /**
     * Bean初始化后自动调用，加载敏感词Trie
     */
    @PostConstruct
    public void init() {
        reload();
    }

    /**
     * 从Redis加载敏感词Trie，并同步更新内存缓存
     */
    public void reload() {
        rwLock.writeLock().lock();
        try {
            // 从loader加载TrieNode根节点
            TrieNode root = loader.load();

            // 构建SensitiveTrie对象，提高匹配效率
            this.trie = new SensitiveTrie(root);

            // 从Trie结构中提取所有词及其级别，更新缓存Map
            this.wordMap = extractWordsFromTrie(root);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 过滤输入文本，返回过滤结果
     * @param content 需要过滤的文本内容
     * @return 过滤结果对象，包含是否阻止发布和替换后的文本
     */
    public SensitiveFilterResult filter(String content) {
        rwLock.readLock().lock();
        try {
            // 在Trie中匹配文本中的敏感词及其等级
            MatchResult result = trie.matchWithLevel(content);

            // 如果含有等级为1（禁止发布）的敏感词，直接阻止发布
            if (result.hasLevel(1)) {
                return SensitiveFilterResult.blocked(result.getWordsByLevel(1));
            }

            // 否则，对等级为2（可替换）的敏感词进行替换，返回允许发布结果
            String replaced = trie.replace(content, result.getWordsByLevel(2), '*');
            return SensitiveFilterResult.allowed(replaced, result.getWordsByLevel(2));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 获取当前所有敏感词及其等级的快照
     * @return 词 -> 等级的映射副本
     */
    public Map<String, Integer> listAll() {
        rwLock.readLock().lock();
        try {
            // 返回新Map，避免外部修改内部缓存
            return new HashMap<>(wordMap);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 新增或更新一个敏感词及其等级
     * @param word  敏感词文本
     * @param level 敏感词等级，1=禁止发布，2=可替换
     */
    public void addWord(String word, int level) {
        rwLock.writeLock().lock();
        try {
            // 更新缓存Map
            wordMap.put(word, level);
            // 重建Trie并同步到Redis，刷新内存Trie
            rebuildTrieAndSave();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 删除一个敏感词
     * @param word 需要删除的敏感词文本
     */
    public void deleteWord(String word) {
        rwLock.writeLock().lock();
        try {
            // 从缓存Map移除
            wordMap.remove(word);
            // 重建Trie并同步到Redis，刷新内存Trie
            rebuildTrieAndSave();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private void rebuildTrieAndSave() {
        TrieNode root = buildTrieFromWordMap(wordMap);
        try {
            // ✅ JSON 序列化
            String json = mapper.writeValueAsString(root);

            // ✅ 使用 StringCodec 明确存为字符串，防止 JDK 二进制序列化
            RedisUtils.getClient()
                    .getBucket(REDIS_KEY, org.redisson.client.codec.StringCodec.INSTANCE)
                    .set(json);
        } catch (Exception e) {
            throw new RuntimeException("序列化敏感词 Trie 失败", e);
        }

        // ✅ 刷新内存中 SensitiveTrie 实例
        this.trie = new SensitiveTrie(root);
    }


    /**
     * 根据词表构建Trie树结构
     * @param words 词 -> 等级映射
     * @return Trie树根节点
     */
    private TrieNode buildTrieFromWordMap(Map<String, Integer> words) {
        TrieNode root = new TrieNode();
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            String word = entry.getKey();
            int level = entry.getValue();
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                // 若子节点不存在则创建
                node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
            }
            node.setEnd(true);     // 标记单词结尾
            node.setLevel(level);  // 设置敏感等级
        }
        return root;
    }

    /**
     * 从Trie树递归提取所有词及其等级，生成词表Map
     * @param root Trie树根节点
     * @return 词 -> 等级映射
     */
    private Map<String, Integer> extractWordsFromTrie(TrieNode root) {
        Map<String, Integer> map = new HashMap<>();
        collectWords(root, new StringBuilder(), map);
        return map;
    }

    /**
     * 辅助递归方法，遍历Trie树，拼接字符构成词
     * @param node 当前节点
     * @param path 当前路径字符串构建器
     * @param map  结果词表
     */
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
