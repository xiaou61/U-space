package com.xiaou.blog.mapper;

import com.xiaou.blog.domain.BlogConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 博客配置Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface BlogConfigMapper {
    
    /**
     * 插入博客配置
     */
    int insert(BlogConfig config);
    
    /**
     * 根据ID查询博客配置
     */
    BlogConfig selectById(Long id);
    
    /**
     * 根据用户ID查询博客配置
     */
    BlogConfig selectByUserId(Long userId);
    
    /**
     * 更新博客配置
     */
    int updateById(BlogConfig config);
    
    /**
     * 根据用户ID更新文章总数
     */
    int updateTotalArticles(@Param("userId") Long userId, @Param("increment") Integer increment);
    
    /**
     * 查询博客总数
     */
    Long selectTotalCount();
    
    /**
     * 查询活跃博主数（近30天发布过文章）
     */
    Long selectActiveBloggersCount();
}

