package com.xiaou.sensitive.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词策略查询DTO
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensitiveStrategyQuery implements PageRequest {

    /**
     * 策略名称（模糊查询）
     */
    private String strategyName;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 风险等级
     */
    private Integer level;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    @Override
    public SensitiveStrategyQuery setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    @Override
    public SensitiveStrategyQuery setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
