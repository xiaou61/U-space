package com.xiaou.sensitive.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词白名单查询DTO
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensitiveWhitelistQuery implements PageRequest {

    /**
     * 白名单词汇（模糊查询）
     */
    private String word;

    /**
     * 分类
     */
    private String category;

    /**
     * 作用范围
     */
    private String scope;

    /**
     * 模块名称
     */
    private String moduleName;

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
    public SensitiveWhitelistQuery setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    @Override
    public SensitiveWhitelistQuery setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
