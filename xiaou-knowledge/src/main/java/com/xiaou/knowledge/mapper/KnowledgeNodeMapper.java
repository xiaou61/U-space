package com.xiaou.knowledge.mapper;

import com.xiaou.knowledge.domain.KnowledgeNode;
import com.xiaou.knowledge.dto.request.CreateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.request.SortNodesRequest;
import com.xiaou.knowledge.dto.request.UpdateKnowledgeNodeRequest;
import com.xiaou.knowledge.dto.response.KnowledgeNodeTreeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识节点Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface KnowledgeNodeMapper {
    
    /**
     * 根据ID查询知识节点
     * 
     * @param id 节点ID
     * @return 知识节点
     */
    KnowledgeNode selectById(Long id);
    
    /**
     * 根据图谱ID查询所有节点(树形结构)
     * 
     * @param mapId 图谱ID
     * @return 节点树形列表
     */
    List<KnowledgeNodeTreeResponse> selectTreeByMapId(Long mapId);
    
    /**
     * 根据父节点ID查询子节点
     * 
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    List<KnowledgeNode> selectByParentId(Long parentId);
    
    /**
     * 查询节点的最大排序序号
     * 
     * @param mapId 图谱ID
     * @param parentId 父节点ID
     * @return 最大排序序号
     */
    Integer selectMaxSortOrder(@Param("mapId") Long mapId, @Param("parentId") Long parentId);
    
    /**
     * 统计图谱节点数量
     * 
     * @param mapId 图谱ID
     * @return 节点数量
     */
    Integer countByMapId(Long mapId);
    
    /**
     * 插入知识节点
     * 
     * @param knowledgeNode 知识节点
     * @return 影响行数
     */
    int insert(KnowledgeNode knowledgeNode);
    
    /**
     * 更新知识节点
     * 
     * @param id 节点ID
     * @param request 更新请求
     * @return 影响行数
     */
    int updateById(@Param("id") Long id, 
                   @Param("request") UpdateKnowledgeNodeRequest request);
    
    /**
     * 批量更新节点排序
     * 
     * @param nodeOrders 节点排序信息
     * @return 影响行数
     */
    int batchUpdateOrder(@Param("nodeOrders") List<SortNodesRequest.NodeOrder> nodeOrders);
    
    /**
     * 增加查看次数
     * 
     * @param id 节点ID
     * @return 影响行数
     */
    int incrementViewCount(Long id);
    
    /**
     * 根据ID删除知识节点
     * 
     * @param id 节点ID
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 根据图谱ID删除所有节点
     * 
     * @param mapId 图谱ID
     * @return 影响行数
     */
    int deleteByMapId(Long mapId);
    
    /**
     * 根据多个图谱ID批量删除所有节点
     * 
     * @param mapIds 图谱ID列表
     * @return 影响行数
     */
    int deleteByMapIds(@Param("mapIds") List<Long> mapIds);
    
    /**
     * 搜索节点
     * 
     * @param mapId 图谱ID
     * @param keyword 关键词
     * @return 节点列表
     */
    List<KnowledgeNodeTreeResponse> searchNodes(@Param("mapId") Long mapId, 
                                               @Param("keyword") String keyword);
} 