package com.xiaou.knowledge.service;

import com.xiaou.knowledge.domain.KnowledgeNode;
import com.xiaou.knowledge.dto.request.CreateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.request.SortNodesRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.response.KnowledgeNodeTreeResponse;

import java.util.List;

/**
 * 知识节点服务接口
 * 
 * @author xiaou
 */
public interface KnowledgeNodeService {
    
    /**
     * 根据ID查询知识节点
     * 
     * @param id 节点ID
     * @return 知识节点
     */
    KnowledgeNode getById(Long id);
    
    /**
     * 根据图谱ID获取节点树
     * 
     * @param mapId 图谱ID
     * @return 节点树
     */
    List<KnowledgeNodeTreeResponse> getTreeByMapId(Long mapId);
    
    /**
     * 创建知识节点
     * 
     * @param mapId 图谱ID
     * @param request 创建请求
     * @return 节点ID
     */
    Long createNode(Long mapId, CreateKnowledgeNodeRequest request);
    
    /**
     * 更新知识节点
     * 
     * @param id 节点ID
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateNode(Long id, UpdateKnowledgeNodeRequest request);
    
    /**
     * 批量更新节点排序
     * 
     * @param mapId 图谱ID
     * @param request 排序请求
     * @return 是否成功
     */
    boolean sortNodes(Long mapId, SortNodesRequest request);
    
    /**
     * 增加查看次数
     * 
     * @param id 节点ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 删除知识节点
     * 
     * @param id 节点ID
     * @return 是否成功
     */
    boolean deleteNode(Long id);
    
    /**
     * 搜索节点
     * 
     * @param mapId 图谱ID
     * @param keyword 关键词
     * @return 节点列表
     */
    List<KnowledgeNodeTreeResponse> searchNodes(Long mapId, String keyword);
    
    /**
     * 构建树形结构
     * 
     * @param nodes 节点列表
     * @return 树形结构
     */
    List<KnowledgeNodeTreeResponse> buildTree(List<KnowledgeNodeTreeResponse> nodes);
} 