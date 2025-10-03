package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.dto.AdminPostQueryRequest;
import com.xiaou.community.dto.CommunityPostQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 社区帖子Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityPostMapper {
    
    /**
     * 插入帖子
     */
    int insert(CommunityPost post);
    
    /**
     * 根据ID查询帖子
     */
    CommunityPost selectById(Long id);
    
    /**
     * 更新帖子
     */
    int updateById(CommunityPost post);
    
    /**
     * 后台查询帖子总数
     */
    Long selectAdminPostCount(AdminPostQueryRequest request);
    
    /**
     * 后台查询帖子列表
     */
    List<CommunityPost> selectAdminPostList(@Param("request") AdminPostQueryRequest request);
    
    /**
     * 置顶帖子
     */
    int topPost(@Param("id") Long id, @Param("topExpireTime") String topExpireTime);
    
    /**
     * 取消置顶
     */
    int cancelTop(Long id);
    
    /**
     * 下架帖子
     */
    int disablePost(Long id);
    
    /**
     * 删除帖子
     */
    int deletePost(Long id);
    
    /**
     * 增加浏览次数
     */
    int incrementViewCount(Long id);
    
    /**
     * 更新点赞数
     */
    int updateLikeCount(@Param("id") Long id, @Param("count") int count);
    
    /**
     * 更新收藏数
     */
    int updateCollectCount(@Param("id") Long id, @Param("count") int count);
    
    /**
     * 更新评论数
     */
    int updateCommentCount(@Param("id") Long id, @Param("count") int count);
    
    /**
     * 根据用户ID查询帖子列表
     */
    List<CommunityPost> selectByUserId(Long userId);
    
    // 前台查询方法
    
    /**
     * 前台查询帖子总数
     */
    Long selectPostCount(@Param("request") CommunityPostQueryRequest request);
    
    /**
     * 前台查询帖子列表
     */
    List<CommunityPost> selectPostList(@Param("request") CommunityPostQueryRequest request);
    
    /**
     * 查询用户收藏的帖子总数
     */
    Long selectUserCollectionCount(@Param("userId") Long userId, @Param("request") CommunityPostQueryRequest request);
    
    /**
     * 查询用户收藏的帖子列表
     */
    List<CommunityPost> selectUserCollectionList(@Param("userId") Long userId, 
                                                 @Param("request") CommunityPostQueryRequest request);
    
    /**
     * 查询用户发布的帖子总数
     */
    Long selectUserPostCount(@Param("userId") Long userId, @Param("request") CommunityPostQueryRequest request);
    
    /**
     * 查询用户发布的帖子列表
     */
    List<CommunityPost> selectUserPostList(@Param("userId") Long userId, 
                                          @Param("request") CommunityPostQueryRequest request);
    
    /**
     * 查询热门帖子（用于热门推荐）
     */
    List<CommunityPost> selectHotPosts(@Param("startTime") Date startTime, 
                                       @Param("minScore") Double minScore,
                                       @Param("limit") Integer limit);
    
    /**
     * 查询用户最近的帖子
     */
    List<CommunityPost> selectRecentPostsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 更新AI摘要信息
     */
    int updateAiSummary(@Param("postId") Long postId, 
                       @Param("aiSummary") String aiSummary, 
                       @Param("aiKeywords") String aiKeywords);
} 