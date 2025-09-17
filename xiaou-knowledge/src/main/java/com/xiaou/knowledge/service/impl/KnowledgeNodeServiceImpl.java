package com.xiaou.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.knowledge.domain.KnowledgeNode;
import com.xiaou.knowledge.dto.request.CreateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.request.SortNodesRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.response.KnowledgeNodeTreeResponse;
import com.xiaou.knowledge.mapper.KnowledgeMapMapper;
import com.xiaou.knowledge.mapper.KnowledgeNodeMapper;
import com.xiaou.knowledge.service.KnowledgeNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识节点服务实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeNodeServiceImpl implements KnowledgeNodeService {
    
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final KnowledgeMapMapper knowledgeMapMapper;
    
    @Override
    public KnowledgeNode getById(Long id) {
        if (id == null) {
            throw new BusinessException("节点ID不能为空");
        }
        KnowledgeNode knowledgeNode = knowledgeNodeMapper.selectById(id);
        if (knowledgeNode == null) {
            throw new BusinessException("知识节点不存在");
        }
        return knowledgeNode;
    }
    
    @Override
    public List<KnowledgeNodeTreeResponse> getTreeByMapId(Long mapId) {
        if (mapId == null) {
            throw new BusinessException("图谱ID不能为空");
        }
        
        List<KnowledgeNodeTreeResponse> nodes = knowledgeNodeMapper.selectTreeByMapId(mapId);
        return buildTree(nodes);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNode(Long mapId, CreateKnowledgeNodeRequest request) {
        if (mapId == null) {
            throw new BusinessException("图谱ID不能为空");
        }
        
        // 检查图谱是否存在
        if (knowledgeMapMapper.selectById(mapId) == null) {
            throw new BusinessException("知识图谱不存在");
        }
        
        // 计算层级深度
        int levelDepth = 1;
        if (request.getParentId() != 0) {
            KnowledgeNode parentNode = knowledgeNodeMapper.selectById(request.getParentId());
            if (parentNode == null) {
                throw new BusinessException("父节点不存在");
            }
            if (!parentNode.getMapId().equals(mapId)) {
                throw new BusinessException("父节点不属于当前图谱");
            }
            levelDepth = parentNode.getLevelDepth() + 1;
        }
        
        // 获取排序序号
        int sortOrder = request.getSortOrder();
        if (sortOrder == 0) {
            Integer maxSortOrder = knowledgeNodeMapper.selectMaxSortOrder(mapId, request.getParentId());
            sortOrder = (maxSortOrder != null ? maxSortOrder : 0) + 1;
        }
        
        KnowledgeNode knowledgeNode = new KnowledgeNode();
        knowledgeNode.setMapId(mapId);
        knowledgeNode.setParentId(request.getParentId());
        knowledgeNode.setTitle(request.getTitle());
        knowledgeNode.setUrl(request.getUrl());
        knowledgeNode.setNodeType(request.getNodeType());
        knowledgeNode.setSortOrder(sortOrder);
        knowledgeNode.setLevelDepth(levelDepth);
        knowledgeNode.setIsExpanded(request.getIsExpanded());
        knowledgeNode.setViewCount(0);
        
        int result = knowledgeNodeMapper.insert(knowledgeNode);
        if (result <= 0) {
            throw new BusinessException("创建知识节点失败");
        }
        
        // 更新图谱节点数量
        updateMapNodeCount(mapId);
        
        log.info("创建知识节点成功，ID: {}, 标题: {}", knowledgeNode.getId(), knowledgeNode.getTitle());
        return knowledgeNode.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNode(Long id, UpdateKnowledgeNodeRequest request) {
        // 检查节点是否存在
        KnowledgeNode knowledgeNode = getById(id);
        
        int result = knowledgeNodeMapper.updateById(id, request);
        if (result > 0) {
            log.info("更新知识节点成功，ID: {}", id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sortNodes(Long mapId, SortNodesRequest request) {
        if (mapId == null) {
            throw new BusinessException("图谱ID不能为空");
        }
        
        // 验证所有节点都属于指定图谱
        for (SortNodesRequest.NodeOrder nodeOrder : request.getNodeOrders()) {
            KnowledgeNode node = getById(nodeOrder.getNodeId());
            if (!node.getMapId().equals(mapId)) {
                throw new BusinessException("节点不属于当前图谱");
            }
        }
        
        int result = knowledgeNodeMapper.batchUpdateOrder(request.getNodeOrders());
        if (result > 0) {
            log.info("批量更新节点排序成功，图谱ID: {}, 节点数量: {}", mapId, result);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean incrementViewCount(Long id) {
        int result = knowledgeNodeMapper.incrementViewCount(id);
        return result > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNode(Long id) {
        // 检查节点是否存在
        KnowledgeNode knowledgeNode = getById(id);
        
        // 检查是否有子节点
        List<KnowledgeNode> children = knowledgeNodeMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("存在子节点，无法删除");
        }
        
        int result = knowledgeNodeMapper.deleteById(id);
        if (result > 0) {
            // 更新图谱节点数量
            updateMapNodeCount(knowledgeNode.getMapId());
            
            log.info("删除知识节点成功，ID: {}", id);
            return true;
        }
        return false;
    }
    
    @Override
    public List<KnowledgeNodeTreeResponse> searchNodes(Long mapId, String keyword) {
        if (mapId == null) {
            throw new BusinessException("图谱ID不能为空");
        }
        
        String trimKeyword = StrUtil.trimToNull(keyword);
        List<KnowledgeNodeTreeResponse> nodes = knowledgeNodeMapper.searchNodes(mapId, trimKeyword);
        return buildTree(nodes);
    }
    
    @Override
    public List<KnowledgeNodeTreeResponse> buildTree(List<KnowledgeNodeTreeResponse> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按父节点ID分组
        Map<Long, List<KnowledgeNodeTreeResponse>> groupedNodes = nodes.stream()
                .collect(Collectors.groupingBy(KnowledgeNodeTreeResponse::getParentId));
        
        // 构建树形结构
        List<KnowledgeNodeTreeResponse> rootNodes = groupedNodes.get(0L);
        if (rootNodes == null) {
            rootNodes = new ArrayList<>();
        }
        
        for (KnowledgeNodeTreeResponse rootNode : rootNodes) {
            buildChildren(rootNode, groupedNodes);
        }
        
        return rootNodes;
    }
    
    /**
     * 递归构建子节点
     */
    private void buildChildren(KnowledgeNodeTreeResponse parentNode, 
                              Map<Long, List<KnowledgeNodeTreeResponse>> groupedNodes) {
        List<KnowledgeNodeTreeResponse> children = groupedNodes.get(parentNode.getId());
        if (children != null && !children.isEmpty()) {
            parentNode.setChildren(children);
            for (KnowledgeNodeTreeResponse child : children) {
                buildChildren(child, groupedNodes);
            }
        }
    }
    
    /**
     * 更新图谱节点数量
     */
    private void updateMapNodeCount(Long mapId) {
        Integer nodeCount = knowledgeNodeMapper.countByMapId(mapId);
        knowledgeMapMapper.updateNodeCount(mapId, nodeCount);
    }
} 