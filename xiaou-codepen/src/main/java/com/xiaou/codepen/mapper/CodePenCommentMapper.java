package com.xiaou.codepen.mapper;

import com.xiaou.codepen.domain.CodePenComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作品评论Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CodePenCommentMapper {
    
    /**
     * 插入评论
     */
    int insert(CodePenComment comment);
    
    /**
     * 根据ID查询评论
     */
    CodePenComment selectById(Long id);
    
    /**
     * 查询作品的评论列表
     */
    List<CodePenComment> selectByPenId(@Param("penId") Long penId, @Param("status") Integer status);
    
    /**
     * 更新评论状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 删除评论
     */
    int deleteById(Long id);
}

