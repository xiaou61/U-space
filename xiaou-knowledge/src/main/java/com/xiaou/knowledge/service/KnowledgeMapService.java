package com.xiaou.knowledge.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.knowledge.domain.KnowledgeMap;
import com.xiaou.knowledge.dto.request.CreateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.KnowledgeMapQueryRequest;
import com.xiaou.knowledge.dto.request.PublishedKnowledgeMapQueryRequest;
import com.xiaou.knowledge.dto.response.KnowledgeMapListResponse;

import java.util.List;

/**
 * 知识图谱服务接口
 * 
 * @author xiaou
 */
public interface KnowledgeMapService {
    
    /**
     * 根据ID查询知识图谱
     * 
     * @param id 图谱ID
     * @return 知识图谱
     */
    KnowledgeMap getById(Long id);
    
    /**
     * 分页查询知识图谱列表(管理员端)
     * 
     * @param request 查询请求参数
     * @return 知识图谱分页列表
     */
    PageResult<KnowledgeMapListResponse> getPageList(KnowledgeMapQueryRequest request);
    
    /**
     * 查询已发布的知识图谱列表(用户端)
     * 
     * @param request 查询请求参数
     * @return 知识图谱分页列表
     */
    PageResult<KnowledgeMapListResponse> getPublishedList(PublishedKnowledgeMapQueryRequest request);
    
    /**
     * 创建知识图谱
     * 
     * @param request 创建请求
     * @param userId 用户ID
     * @return 图谱ID
     */
    Long createMap(CreateKnowledgeMapRequest request, Long userId);
    
    /**
     * 更新知识图谱
     * 
     * @param id 图谱ID
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateMap(Long id, UpdateKnowledgeMapRequest request);
    
    /**
     * 发布图谱
     * 
     * @param id 图谱ID
     * @return 是否成功
     */
    boolean publishMap(Long id);
    
    /**
     * 隐藏图谱
     * 
     * @param id 图谱ID
     * @return 是否成功
     */
    boolean hideMap(Long id);
    
    /**
     * 增加查看次数
     * 
     * @param id 图谱ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);
    
    /**
     * 更新节点数量
     * 
     * @param mapId 图谱ID
     * @return 是否成功
     */
    boolean updateNodeCount(Long mapId);
    
    /**
     * 删除知识图谱
     * 
     * @param id 图谱ID
     * @return 是否成功
     */
    boolean deleteMap(Long id);
    
    /**
     * 批量删除知识图谱
     * 
     * @param ids 图谱ID列表
     * @return 是否成功
     */
    boolean deleteBatch(List<Long> ids);
} 