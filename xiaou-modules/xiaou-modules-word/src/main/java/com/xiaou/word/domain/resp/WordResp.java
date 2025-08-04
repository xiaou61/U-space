package com.xiaou.word.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.word.domain.entity.Word;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;


@Data
@AutoMapper(target = Word.class)
public class WordResp {

    private String id;

    /**
     * 单词
     */
    private String word;

    /**
     * 音标
     */
    private String phonetic;

    /**
     * 词性，例如 n. v. adj.
     */
    private String partOfSpeech;

    /**
     * 中文释义
     */
    private String definition;

    /**
     * 英文释义
     */
    private String definitionEn;

    /**
     * 例句
     */
    private String exampleSentence;

    /**
     * 词库来源，如 CET-4、GRE
     */
    private String source;

    /**
     * 标签，逗号分隔，如 常考,高频
     */
    private String tags;

    /**
     * 难度等级：1-5
     */
    private Integer difficulty;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}