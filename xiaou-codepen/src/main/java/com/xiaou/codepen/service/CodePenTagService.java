package com.xiaou.codepen.service;

import com.xiaou.codepen.domain.CodePenTag;

import java.util.List;

/**
 * 作品标签Service
 * 
 * @author xiaou
 */
public interface CodePenTagService {
    
    /**
     * 获取所有标签
     */
    List<CodePenTag> getAllTags();
    
    /**
     * 获取热门标签
     */
    List<CodePenTag> getHotTags(Integer limit);
    
    /**
     * 创建标签
     */
    Long createTag(String tagName, String tagDescription);
    
    /**
     * 更新标签
     */
    boolean updateTag(Long id, String tagName, String tagDescription);
    
    /**
     * 删除标签
     */
    boolean deleteTag(Long id);
    
    /**
     * 合并标签
     */
    boolean mergeTags(Long sourceId, Long targetId);
}

