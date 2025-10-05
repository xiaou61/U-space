package com.xiaou.sensitive.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 趋势数据VO
 *
 * @author xiaou
 */
@Data
public class TrendDataVO {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 业务模块
     */
    private String module;
}
