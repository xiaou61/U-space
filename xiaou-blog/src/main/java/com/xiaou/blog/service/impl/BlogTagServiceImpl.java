package com.xiaou.blog.service.impl;

import com.xiaou.common.utils.PageHelper;
import com.xiaou.blog.domain.BlogTag;
import com.xiaou.blog.mapper.BlogTagMapper;
import com.xiaou.blog.service.BlogTagService;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 博客标签Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogTagServiceImpl implements BlogTagService {
    
    private final BlogTagMapper blogTagMapper;
    
    @Override
    public List<BlogTag> getAllTags() {
        return blogTagMapper.selectAll();
    }
    
    @Override
    public PageResult<BlogTag> getTagList(Integer pageNum, Integer pageSize) {
        return PageHelper.doPage(pageNum, pageSize, () -> 
            blogTagMapper.selectAll()
        );
    }
    
    @Override
    public List<BlogTag> getHotTags(Integer limit) {
        return blogTagMapper.selectHotTags(limit);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeTags(Long sourceTagId, Long targetTagId) {
        BlogTag sourceTag = blogTagMapper.selectById(sourceTagId);
        BlogTag targetTag = blogTagMapper.selectById(targetTagId);
        
        if (sourceTag == null || targetTag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // TODO: 将使用源标签的文章改为使用目标标签
        // 这需要更新blog_article表中的tags字段
        
        // 更新目标标签使用次数
        blogTagMapper.updateUseCount(targetTagId, targetTag.getUseCount() + sourceTag.getUseCount());
        
        // 删除源标签
        blogTagMapper.deleteById(sourceTagId);
        
        log.info("合并标签成功，源标签：{}，目标标签：{}", sourceTagId, targetTagId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        BlogTag tag = blogTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 可以选择检查标签是否还在使用
        if (tag.getUseCount() != null && tag.getUseCount() > 0) {
            throw new BusinessException("该标签还在使用中，无法删除");
        }
        
        blogTagMapper.deleteById(id);
        
        log.info("删除标签成功：{}", id);
    }
}

