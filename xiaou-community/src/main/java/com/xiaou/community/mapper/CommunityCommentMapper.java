package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityComment;
import com.xiaou.community.dto.AdminCommentQueryRequest;
import com.xiaou.community.dto.CommunityCommentQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社区评论Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityCommentMapper {
    
    /**
     * 插入评论
     */
    int insert(CommunityComment comment);
    
    /**
     * 根据ID查询评论
     */
    CommunityComment selectById(Long id);
    
    /**
     * 更新评论
     */
    int updateById(CommunityComment comment);
    
    /**
     * 后台查询评论总数
     */
    Long selectAdminCommentCount(AdminCommentQueryRequest request);
    
    /**
     * 后台查询评论列表
     */
    List<CommunityComment> selectAdminCommentList(@Param("request") AdminCommentQueryRequest request);
    
    /**
     * 删除评论
     */
    int deleteComment(Long id);
    
    /**
     * 根据帖子ID查询评论
     */
    List<CommunityComment> selectByPostId(@Param("postId") Long postId, @Param("sort") String sort);
    
    /**
     * 更新点赞数
     */
    int updateLikeCount(@Param("id") Long id, @Param("count") int count);
    
    /**
     * 根据用户ID查询评论列表
     */
    List<CommunityComment> selectByUserId(Long userId);
    
    // 前台查询方法
    
    /**
     * 查询帖子的评论总数
     */
    Long selectPostCommentCount(@Param("postId") Long postId, @Param("request") CommunityCommentQueryRequest request);
    
    /**
     * 分页查询帖子的评论列表
     */
    List<CommunityComment> selectPostCommentList(@Param("postId") Long postId, 
                                                @Param("request") CommunityCommentQueryRequest request);
    
    /**
     * 查询用户的评论总数
     */
    Long selectUserCommentCount(@Param("userId") Long userId, @Param("request") CommunityCommentQueryRequest request);
    
    /**
     * 分页查询用户的评论列表
     */
    List<CommunityComment> selectUserCommentList(@Param("userId") Long userId, 
                                                @Param("request") CommunityCommentQueryRequest request);
} 