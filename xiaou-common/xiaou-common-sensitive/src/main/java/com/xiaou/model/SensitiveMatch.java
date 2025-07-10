package com.xiaou.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensitiveMatch {
    private String word;
    private int level;
}
