package com.xiaou.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.knowledge.domain.KnowledgeMap;
import com.xiaou.knowledge.dto.request.CreateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.KnowledgeMapQueryRequest;
import com.xiaou.knowledge.dto.request.PublishedKnowledgeMapQueryRequest;
import com.xiaou.knowledge.dto.response.KnowledgeMapListResponse;
import com.xiaou.knowledge.mapper.KnowledgeMapMapper;
import com.xiaou.knowledge.mapper.KnowledgeNodeMapper;
import com.xiaou.knowledge.service.KnowledgeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识图谱服务实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeMapServiceImpl implements KnowledgeMapService {
    
    private final KnowledgeMapMapper knowledgeMapMapper;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    
    @Override
    public KnowledgeMap getById(Long id) {
        if (id == null) {
            throw new BusinessException("图谱ID不能为空");
        }
        KnowledgeMap knowledgeMap = knowledgeMapMapper.selectById(id);
        if (knowledgeMap == null) {
            throw new BusinessException("知识图谱不存在");
        }
        return knowledgeMap;
    }
    
    @Override
    public PageResult<KnowledgeMapListResponse> getPageList(KnowledgeMapQueryRequest request) {
        log.info("分页查询知识图谱列表，查询条件: {}", request);
        
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            String trimKeyword = StrUtil.trimToNull(request.getKeyword());
            List<KnowledgeMapListResponse> list = knowledgeMapMapper.selectPageList(trimKeyword, request.getStatus());
            log.info("查询到图谱数量: {}", list.size());
            return list;
        });
    }
    
    @Override
    public PageResult<KnowledgeMapListResponse> getPublishedList(PublishedKnowledgeMapQueryRequest request) {
        log.info("分页查询已发布知识图谱列表，查询条件: {}", request);
        
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            String trimKeyword = StrUtil.trimToNull(request.getKeyword());
            List<KnowledgeMapListResponse> list = knowledgeMapMapper.selectPublishedList(trimKeyword);
            log.info("查询到已发布图谱数量: {}", list.size());
            return list;
        });
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMap(CreateKnowledgeMapRequest request, Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        KnowledgeMap knowledgeMap = new KnowledgeMap();
        knowledgeMap.setTitle(request.getTitle());
        knowledgeMap.setDescription(request.getDescription());
        knowledgeMap.setCoverImage(request.getCoverImage());
        knowledgeMap.setUserId(userId);
        knowledgeMap.setNodeCount(0);
        knowledgeMap.setViewCount(0);
        knowledgeMap.setStatus(request.getStatus() != null ? request.getStatus() : KnowledgeMap.Status.DRAFT.getCode());
        knowledgeMap.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
        int result = knowledgeMapMapper.insert(knowledgeMap);
        if (result <= 0) {
            throw new BusinessException("创建知识图谱失败");
        }
        
        log.info("创建知识图谱成功，ID: {}, 标题: {}", knowledgeMap.getId(), knowledgeMap.getTitle());
        return knowledgeMap.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMap(Long id, UpdateKnowledgeMapRequest request) {
        // 检查图谱是否存在
        getById(id);
        
        int result = knowledgeMapMapper.updateById(id, request);
        if (result > 0) {
            log.info("更新知识图谱成功，ID: {}", id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishMap(Long id) {
        // 检查图谱是否存在
        getById(id);
        
        int result = knowledgeMapMapper.updateStatus(id, KnowledgeMap.Status.PUBLISHED.getCode());
        if (result > 0) {
            log.info("发布知识图谱成功，ID: {}", id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean hideMap(Long id) {
        // 检查图谱是否存在
        getById(id);
        
        int result = knowledgeMapMapper.updateStatus(id, KnowledgeMap.Status.HIDDEN.getCode());
        if (result > 0) {
            log.info("隐藏知识图谱成功，ID: {}", id);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean incrementViewCount(Long id) {
        int result = knowledgeMapMapper.incrementViewCount(id);
        return result > 0;
    }
    
    @Override
    public boolean updateNodeCount(Long mapId) {
        Integer nodeCount = knowledgeNodeMapper.countByMapId(mapId);
        int result = knowledgeMapMapper.updateNodeCount(mapId, nodeCount);
        return result > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMap(Long id) {
        // 检查图谱是否存在
        getById(id);
        
        // 先删除相关节点
        knowledgeNodeMapper.deleteByMapId(id);
        
        // 再删除图谱
        int result = knowledgeMapMapper.deleteById(id);
        if (result > 0) {
            log.info("删除知识图谱成功，ID: {}", id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除的图谱ID列表不能为空");
        }
        
        // 使用批量删除相关节点，避免N+1问题
        knowledgeNodeMapper.deleteByMapIds(ids);
        
        // 再批量删除图谱
        int result = knowledgeMapMapper.deleteBatchIds(ids);
        if (result > 0) {
            log.info("批量删除知识图谱成功，数量: {}", result);
            return true;
        }
        return false;
    }
} 