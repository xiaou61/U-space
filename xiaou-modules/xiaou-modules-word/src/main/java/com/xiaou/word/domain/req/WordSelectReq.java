package com.xiaou.word.domain.req;

import lombok.Data;

@Data
public class WordSelectReq {
    /**
     * 单词id
     */
    private String wordId;

    /**
     * 1为认识 2为不认识
     */
    private Integer type;
}
