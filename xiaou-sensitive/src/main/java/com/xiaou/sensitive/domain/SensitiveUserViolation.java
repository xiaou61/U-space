package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户违规统计实体
 *
 * @author xiaou
 */
@Data
public class SensitiveUserViolation {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 违规次数
     */
    private Integer violationCount;

    /**
     * 最后违规时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastViolationTime;

    /**
     * 是否被限制 0-否 1-是
     */
    private Integer isRestricted;

    /**
     * 限制结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime restrictEndTime;
}
