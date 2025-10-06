package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词处理策略实体
 *
 * @author xiaou
 */
@Data
public class SensitiveStrategy {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 策略名称
     */
    private String strategyName;

    /**
     * 业务模块（community/interview/moment等）
     */
    private String module;

    /**
     * 风险等级 1-低 2-中 3-高
     */
    private Integer level;

    /**
     * 处理动作（replace/reject/warn）
     */
    private String action;

    /**
     * 是否通知管理员 0-否 1-是
     */
    private Integer notifyAdmin;

    /**
     * 是否限制用户 0-否 1-是
     */
    private Integer limitUser;

    /**
     * 限制时长（分钟）
     */
    private Integer limitDuration;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 状态 0-禁用 1-启用
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
}
