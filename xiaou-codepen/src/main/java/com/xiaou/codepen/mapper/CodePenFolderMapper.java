package com.xiaou.codepen.mapper;

import com.xiaou.codepen.domain.CodePenFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏夹Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CodePenFolderMapper {
    
    /**
     * 插入收藏夹
     */
    int insert(CodePenFolder folder);
    
    /**
     * 根据ID查询收藏夹
     */
    CodePenFolder selectById(Long id);
    
    /**
     * 查询用户的收藏夹列表
     */
    List<CodePenFolder> selectByUserId(Long userId);
    
    /**
     * 更新收藏夹
     */
    int updateById(CodePenFolder folder);
    
    /**
     * 更新收藏数量
     */
    int updateCollectCount(@Param("id") Long id, @Param("increment") Integer increment);
    
    /**
     * 删除收藏夹
     */
    int deleteById(Long id);
}

