package com.xiaou.sensitive.dto;


import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensitiveWordQuery implements PageRequest {
    /**
     * 敏感词（模糊查询）
     */
    private String word;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 风险等级
     */
    private Integer level;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public SensitiveWordQuery setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public SensitiveWordQuery setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}