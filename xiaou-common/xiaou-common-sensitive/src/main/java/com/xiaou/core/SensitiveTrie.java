package com.xiaou.core;

import com.xiaou.model.TrieNode;
import com.xiaou.model.MatchResult;
import java.util.List;
import java.util.regex.Pattern;

public class SensitiveTrie {
    private final TrieNode root;

    public SensitiveTrie(TrieNode root) {
        this.root = root;
    }

    public MatchResult matchWithLevel(String text) {
        MatchResult result = new MatchResult();
        for (int i = 0; i < text.length(); i++) {
            TrieNode node = root;
            int j = i;
            StringBuilder sb = new StringBuilder();
            while (j < text.length() && node.getChildren().containsKey(text.charAt(j))) {
                char c = text.charAt(j);
                node = node.getChildren().get(c);
                sb.append(c);
                if (node.isEnd()) {
                    result.add(sb.toString(), node.getLevel());
                }
                j++;
            }
        }
        return result;
    }

    public String replace(String text, List<String> wordsToReplace, char c) {
        for (String word : wordsToReplace) {
            text = text.replaceAll(Pattern.quote(word), repeat(c, word.length()));
        }
        return text;
    }

    private String repeat(char c, int count) {
        return String.valueOf(c).repeat(Math.max(0, count));
    }
}
