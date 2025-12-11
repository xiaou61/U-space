package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 闪卡实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class Flashcard {

    /**
     * 闪卡ID
     */
    private Long id;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 所属卡组ID
     */
    private Long deckId;

    /**
     * 关联的面试题ID
     */
    private Long questionId;

    /**
     * 正面内容(问题)
     */
    private String frontContent;

    /**
     * 背面内容(答案)
     */
    private String backContent;

    /**
     * 来源类型: 1-题库生成 2-用户创建
     */
    private Integer sourceType;

    /**
     * 状态: 0-禁用 1-正常
     */
    private Integer status;

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

    // ====== 扩展字段（非数据库字段）======

    /**
     * 卡组名称
     */
    private String deckName;
}
