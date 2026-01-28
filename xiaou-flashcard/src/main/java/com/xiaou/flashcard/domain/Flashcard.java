package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闪卡实体类
 *
 * @author xiaou
 */
@Data
public class Flashcard {

    /**
     * 闪卡ID
     */
    private Long id;

    /**
     * 所属卡组ID
     */
    private Long deckId;

    /**
     * 正面内容(问题)
     */
    private String frontContent;

    /**
     * 反面内容(答案)
     */
    private String backContent;

    /**
     * 内容类型: 1-文本 2-Markdown 3-代码
     */
    private Integer contentType;

    /**
     * 标签
     */
    private String tags;

    /**
     * 关联题库题目ID
     */
    private Long sourceQuestionId;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 删除标志: 0-正常 1-已删除
     */
    private Integer delFlag;
}
