package com.xiaou.blog.mapper;

import com.xiaou.blog.domain.BlogTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface BlogTagMapper {
    
    /**
     * 插入标签
     */
    int insert(BlogTag tag);
    
    /**
     * 根据ID查询标签
     */
    BlogTag selectById(Long id);
    
    /**
     * 根据标签名查询
     */
    BlogTag selectByTagName(String tagName);
    
    /**
     * 更新标签
     */
    int updateById(BlogTag tag);
    
    /**
     * 删除标签
     */
    int deleteById(Long id);
    
    /**
     * 查询所有标签
     */
    List<BlogTag> selectAll();
    
    /**
     * 查询热门标签
     */
    List<BlogTag> selectHotTags(@Param("limit") Integer limit);
    
    /**
     * 更新标签使用次数（通过ID）
     */
    int updateUseCount(@Param("id") Long id, @Param("increment") Integer increment);
    
    /**
     * 增加标签使用次数（通过标签名）
     */
    int incrementUseCount(@Param("tagName") String tagName);
    
    /**
     * 批量插入或更新标签
     */
    int insertBatch(List<String> tagNames);
}

