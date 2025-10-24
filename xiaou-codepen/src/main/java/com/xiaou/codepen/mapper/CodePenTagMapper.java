package com.xiaou.codepen.mapper;

import com.xiaou.codepen.domain.CodePenTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作品标签Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CodePenTagMapper {
    
    /**
     * 插入标签
     */
    int insert(CodePenTag tag);
    
    /**
     * 根据ID查询标签
     */
    CodePenTag selectById(Long id);
    
    /**
     * 根据标签名称查询
     */
    CodePenTag selectByName(String tagName);
    
    /**
     * 查询所有标签
     */
    List<CodePenTag> selectAll();
    
    /**
     * 查询热门标签
     */
    List<CodePenTag> selectHotTags(@Param("limit") Integer limit);
    
    /**
     * 更新标签
     */
    int updateById(CodePenTag tag);
    
    /**
     * 增加标签使用次数
     */
    int incrementUseCount(Long id);
    
    /**
     * 删除标签
     */
    int deleteById(Long id);
    
    /**
     * 合并标签（将sourceId的使用次数加到targetId上）
     */
    int mergeTags(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}

