package com.xiaou.codepen.service.impl;

import com.xiaou.codepen.domain.CodePenTag;
import com.xiaou.codepen.mapper.CodePenTagMapper;
import com.xiaou.codepen.service.CodePenTagService;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 作品标签Service实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodePenTagServiceImpl implements CodePenTagService {
    
    private final CodePenTagMapper tagMapper;
    
    @Override
    public List<CodePenTag> getAllTags() {
        return tagMapper.selectAll();
    }
    
    @Override
    public List<CodePenTag> getHotTags(Integer limit) {
        return tagMapper.selectHotTags(limit);
    }
    
    @Override
    public Long createTag(String tagName, String tagDescription) {
        // 检查标签是否已存在
        CodePenTag existTag = tagMapper.selectByName(tagName);
        if (existTag != null) {
            throw new BusinessException("标签已存在");
        }
        
        CodePenTag tag = new CodePenTag();
        tag.setTagName(tagName);
        tag.setTagDescription(tagDescription);
        tagMapper.insert(tag);
        
        return tag.getId();
    }
    
    @Override
    public boolean updateTag(Long id, String tagName, String tagDescription) {
        CodePenTag tag = new CodePenTag();
        tag.setId(id);
        tag.setTagName(tagName);
        tag.setTagDescription(tagDescription);
        return tagMapper.updateById(tag) > 0;
    }
    
    @Override
    public boolean deleteTag(Long id) {
        return tagMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mergeTags(Long sourceId, Long targetId) {
        if (sourceId == null || targetId == null) {
            throw new BusinessException("源标签ID和目标标签ID不能为空");
        }
        if (sourceId.equals(targetId)) {
            throw new BusinessException("源标签和目标标签不能相同");
        }
        
        // 检查两个标签是否存在
        com.xiaou.codepen.domain.CodePenTag sourceTag = tagMapper.selectById(sourceId);
        com.xiaou.codepen.domain.CodePenTag targetTag = tagMapper.selectById(targetId);
        
        if (sourceTag == null) {
            throw new BusinessException("源标签不存在");
        }
        if (targetTag == null) {
            throw new BusinessException("目标标签不存在");
        }
        
        // 合并标签（将源标签的使用次数加到目标标签上）
        tagMapper.mergeTags(sourceId, targetId);
        
        // 删除源标签
        tagMapper.deleteById(sourceId);
        
        log.info("标签合并成功，源标签ID：{}，目标标签ID：{}", sourceId, targetId);
        
        return true;
    }
}

