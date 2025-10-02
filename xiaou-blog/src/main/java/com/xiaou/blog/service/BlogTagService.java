package com.xiaou.blog.service;

import com.xiaou.blog.domain.BlogTag;
import com.xiaou.common.core.domain.PageResult;

import java.util.List;

/**
 * 博客标签Service接口
 * 
 * @author xiaou
 */
public interface BlogTagService {
    
    /**
     * 获取所有标签列表
     */
    List<BlogTag> getAllTags();
    
    /**
     * 分页获取标签列表
     */
    PageResult<BlogTag> getTagList(Integer pageNum, Integer pageSize);
    
    /**
     * 获取热门标签
     */
    List<BlogTag> getHotTags(Integer limit);
    
    /**
     * 合并标签
     */
    void mergeTags(Long sourceTagId, Long targetTagId);
    
    /**
     * 删除标签
     */
    void deleteTag(Long id);
}

