package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 同音字映射实体
 *
 * @author xiaou
 */
@Data
public class SensitiveHomophone {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 原始字符
     */
    private String originalChar;

    /**
     * 同音字（逗号分隔）
     */
    private String homophoneChars;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
