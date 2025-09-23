package com.xiaou.knowledge.mapper;

import com.xiaou.knowledge.domain.KnowledgeMap;
import com.xiaou.knowledge.dto.request.CreateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeMapRequest;
import com.xiaou.knowledge.dto.response.KnowledgeMapListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识图谱Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface KnowledgeMapMapper {
    
    /**
     * 根据ID查询知识图谱
     * 
     * @param id 图谱ID
     * @return 知识图谱
     */
    KnowledgeMap selectById(Long id);
    
    /**
     * 分页查询知识图谱列表
     * 
     * @param keyword 搜索关键词
     * @param status 状态
     * @return 知识图谱列表
     */
    List<KnowledgeMapListResponse> selectPageList(@Param("keyword") String keyword, 
                                                  @Param("status") Integer status);
    
    /**
     * 查询已发布的知识图谱列表(用户端)
     * 
     * @param keyword 搜索关键词
     * @return 知识图谱列表
     */
    List<KnowledgeMapListResponse> selectPublishedList(@Param("keyword") String keyword);
    
    /**
     * 插入知识图谱
     * 
     * @param knowledgeMap 知识图谱
     * @return 影响行数
     */
    int insert(KnowledgeMap knowledgeMap);
    
    /**
     * 更新知识图谱
     * 
     * @param id 图谱ID
     * @param request 更新请求
     * @return 影响行数
     */
    int updateById(@Param("id") Long id, 
                   @Param("request") UpdateKnowledgeMapRequest request);
    
    /**
     * 更新图谱状态
     * 
     * @param id 图谱ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 增加查看次数
     * 
     * @param id 图谱ID
     * @return 影响行数
     */
    int incrementViewCount(Long id);
    
    /**
     * 更新节点数量
     * 
     * @param mapId 图谱ID
     * @param nodeCount 节点数量
     * @return 影响行数
     */
    int updateNodeCount(@Param("mapId") Long mapId, @Param("nodeCount") Integer nodeCount);
    
    /**
     * 根据ID删除知识图谱
     * 
     * @param id 图谱ID
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 批量删除知识图谱
     * 
     * @param ids 图谱ID列表
     * @return 影响行数
     */
    int deleteBatchIds(@Param("ids") List<Long> ids);
} 