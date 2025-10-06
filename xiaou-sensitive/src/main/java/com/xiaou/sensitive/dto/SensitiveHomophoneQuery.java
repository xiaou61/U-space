package com.xiaou.sensitive.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 同音字映射查询DTO
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensitiveHomophoneQuery implements PageRequest {

    /**
     * 原始字符（模糊查询）
     */
    private String originalChar;

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
    public SensitiveHomophoneQuery setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    @Override
    public SensitiveHomophoneQuery setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
