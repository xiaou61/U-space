package com.xiaou.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MatchResult {
    private List<SensitiveMatch> matchedWords = new ArrayList<>();

    public void add(String word, int level) {
        matchedWords.add(new SensitiveMatch(word, level));
    }

    public boolean hasLevel(int level) {
        return matchedWords.stream().anyMatch(m -> m.getLevel() == level);
    }

    public List<String> getWordsByLevel(int level) {
        return matchedWords.stream()
                .filter(m -> m.getLevel() == level)
                .map(SensitiveMatch::getWord)
                .collect(Collectors.toList());
    }
}
