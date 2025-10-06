package com.xiaou.sensitive.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词来源查询DTO
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensitiveSourceQuery implements PageRequest {

    /**
     * 来源名称（模糊查询）
     */
    private String sourceName;

    /**
     * 来源类型
     */
    private String sourceType;

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
    public SensitiveSourceQuery setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    @Override
    public SensitiveSourceQuery setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
