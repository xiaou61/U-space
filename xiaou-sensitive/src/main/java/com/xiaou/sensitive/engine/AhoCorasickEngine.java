package com.xiaou.sensitive.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * AC自动机敏感词检测引擎
 * 使用Aho-Corasick算法实现多模式字符串匹配
 */
@Slf4j
@Component
public class AhoCorasickEngine implements SensitiveEngine {

    private TrieNode root;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 性能保护参数
    private static final int MAX_TEXT_LENGTH = 10000; // 最大文本长度
    private static final int MAX_PATTERN_COUNT = 50000; // 最大敏感词数量
    private static final int MAX_PATTERN_LENGTH = 100; // 最大敏感词长度

    /**
     * Trie树节点
     */
    private static class TrieNode {
        private final Map<Character, TrieNode> children = new HashMap<>();
        private TrieNode failure;
        private boolean isEndOfWord;
        private String pattern; // 只存储一个模式，避免HashSet开销
        
        public void addChild(char c, TrieNode node) {
            children.put(c, node);
        }
        
        public TrieNode getChild(char c) {
            return children.get(c);
        }
        
        public boolean hasChild(char c) {
            return children.containsKey(c);
        }
        
        public Collection<TrieNode> getChildren() {
            return children.values();
        }
        
        public void setFailure(TrieNode failure) {
            this.failure = failure;
        }
        
        public TrieNode getFailure() {
            return failure;
        }
        
        public void setPattern(String pattern) {
            this.pattern = pattern;
            this.isEndOfWord = true;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public boolean isEndOfPattern() {
            return isEndOfWord;
        }
    }

    @Override
    public void initialize(Set<String> sensitiveWords) {
        lock.writeLock().lock();
        try {
            // 检查敏感词数量限制
            if (sensitiveWords.size() > MAX_PATTERN_COUNT) {
                log.warn("敏感词数量过多：{}，超过限制：{}，建议优化", sensitiveWords.size(), MAX_PATTERN_COUNT);
            }
            
            // 过滤无效敏感词
            Set<String> filteredWords = new HashSet<>();
            for (String word : sensitiveWords) {
                if (word != null && !word.trim().isEmpty() && word.length() <= MAX_PATTERN_LENGTH) {
                    filteredWords.add(word.trim().toLowerCase()); // 转小写，统一处理
                }
            }
            
            buildTrie(filteredWords);
            buildFailurePointers();
            log.info("AC自动机初始化完成，有效敏感词数量: {}", filteredWords.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<String> findSensitiveWords(String text) {
        if (text == null || text.isEmpty() || root == null) {
            return new ArrayList<>();
        }
        
        // 文本长度保护
        if (text.length() > MAX_TEXT_LENGTH) {
            log.warn("文本长度过长：{}，截取前{}个字符进行检测", text.length(), MAX_TEXT_LENGTH);
            text = text.substring(0, MAX_TEXT_LENGTH);
        }
        
        lock.readLock().lock();
        try {
            Set<String> foundWords = new HashSet<>();
            TrieNode current = root;
            String lowerText = text.toLowerCase(); // 转小写匹配
            
            for (int i = 0; i < lowerText.length(); i++) {
                char c = lowerText.charAt(i);
                
                // 优化的failure指针跳转，避免无限循环
                while (current != root && !current.hasChild(c)) {
                    current = current.getFailure();
                    // 防止failure指针循环
                    if (current == null) {
                        current = root;
                        break;
                    }
                }
                
                // 如果找到匹配的字符，移动到子节点
                if (current.hasChild(c)) {
                    current = current.getChild(c);
                }
                
                // 检查匹配，限制failure链遍历深度
                TrieNode temp = current;
                int depth = 0;
                while (temp != null && depth < 10) { // 限制遍历深度，防止性能问题
                    if (temp.isEndOfPattern()) {
                        foundWords.add(temp.getPattern());
                    }
                    temp = temp.getFailure();
                    depth++;
                }
            }
            
            return new ArrayList<>(foundWords);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String replaceSensitiveWords(String text, String replacement) {
        if (text == null || text.isEmpty() || root == null) {
            return text;
        }
        
        // 文本长度保护
        if (text.length() > MAX_TEXT_LENGTH) {
            log.warn("文本长度过长：{}，截取前{}个字符进行处理", text.length(), MAX_TEXT_LENGTH);
            text = text.substring(0, MAX_TEXT_LENGTH);
        }
        
        if (replacement == null) {
            replacement = "***";
        }
        
        lock.readLock().lock();
        try {
            // 先找到所有敏感词位置
            List<MatchResult> matches = findMatches(text);
            if (matches.isEmpty()) {
                return text;
            }
            
            // 按位置排序，从后往前替换，避免位置偏移
            matches.sort((a, b) -> Integer.compare(b.start, a.start));
            
            StringBuilder result = new StringBuilder(text);
            for (MatchResult match : matches) {
                result.replace(match.start, match.end, replacement);
            }
            
            return result.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsSensitiveWords(String text) {
        if (text == null || text.isEmpty() || root == null) {
            return false;
        }
        
        // 文本长度保护
        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }
        
        lock.readLock().lock();
        try {
            TrieNode current = root;
            String lowerText = text.toLowerCase();
            
            for (int i = 0; i < lowerText.length(); i++) {
                char c = lowerText.charAt(i);
                
                while (current != root && !current.hasChild(c)) {
                    current = current.getFailure();
                    if (current == null) {
                        current = root;
                        break;
                    }
                }
                
                if (current.hasChild(c)) {
                    current = current.getChild(c);
                }
                
                // 快速检查，一旦发现就返回
                TrieNode temp = current;
                int depth = 0;
                while (temp != null && depth < 10) {
                    if (temp.isEndOfPattern()) {
                        return true;
                    }
                    temp = temp.getFailure();
                    depth++;
                }
            }
            
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void refresh(Set<String> sensitiveWords) {
        initialize(sensitiveWords);
    }

    /**
     * 构建Trie树
     */
    private void buildTrie(Set<String> patterns) {
        root = new TrieNode();
        
        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }
            
            TrieNode current = root;
            for (char c : pattern.toCharArray()) {
                if (!current.hasChild(c)) {
                    current.addChild(c, new TrieNode());
                }
                current = current.getChild(c);
            }
            current.setPattern(pattern);
        }
    }

    /**
     * 构建failure指针 - 优化版本，避免循环引用
     */
    private void buildFailurePointers() {
        Queue<TrieNode> queue = new LinkedList<>();
        
        // 第一层的failure指针都指向根节点
        for (TrieNode child : root.getChildren()) {
            child.setFailure(root);
            queue.offer(child);
        }
        
        // BFS构建failure指针
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            
            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();
                queue.offer(child);
                
                // 查找failure指针，增加循环保护
                TrieNode failure = current.getFailure();
                int steps = 0;
                while (failure != null && !failure.hasChild(c) && steps < 100) {
                    failure = failure.getFailure();
                    steps++;
                }
                
                if (failure != null && failure.hasChild(c)) {
                    child.setFailure(failure.getChild(c));
                } else {
                    child.setFailure(root);
                }
                
                // 避免循环引用
                if (child.getFailure() == child) {
                    child.setFailure(root);
                }
            }
        }
    }

    /**
     * 查找所有匹配位置
     */
    private List<MatchResult> findMatches(String text) {
        List<MatchResult> matches = new ArrayList<>();
        TrieNode current = root;
        String lowerText = text.toLowerCase();
        
        for (int i = 0; i < lowerText.length(); i++) {
            char c = lowerText.charAt(i);
            
            while (current != root && !current.hasChild(c)) {
                current = current.getFailure();
                if (current == null) {
                    current = root;
                    break;
                }
            }
            
            if (current.hasChild(c)) {
                current = current.getChild(c);
            }
            
            TrieNode temp = current;
            int depth = 0;
            while (temp != null && depth < 10) {
                if (temp.isEndOfPattern()) {
                    String pattern = temp.getPattern();
                    int start = i - pattern.length() + 1;
                    matches.add(new MatchResult(start, i + 1, pattern));
                }
                temp = temp.getFailure();
                depth++;
            }
        }
        
        return matches;
    }

    /**
     * 匹配结果
     */
    private static class MatchResult {
        final int start;
        final int end;
        final String pattern;
        
        MatchResult(int start, int end, String pattern) {
            this.start = start;
            this.end = end;
            this.pattern = pattern;
        }
    }
} 