package com.xiaou.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrieNode implements Serializable {
    private boolean isEnd = false;
    private int level = 1; // 1=禁止发布，2=可替换
    private Map<Character, TrieNode> children = new HashMap<>();
}
