package com.xiaou.mockinterview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 模拟面试方向配置实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class MockInterviewDirection {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 方向代码（java/frontend等）
     */
    private String directionCode;

    /**
     * 方向名称
     */
    private String directionName;

    /**
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联的题库分类ID（逗号分隔）
     */
    private String categoryIds;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // =================== 查询时使用的非数据库字段 ===================

    /**
     * 面试次数（查询时使用）
     */
    private Integer interviewCount;
}
