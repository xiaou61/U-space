package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.dto.AdminUserQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户社区状态Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityUserStatusMapper {
    
    /**
     * 插入用户状态
     */
    int insert(CommunityUserStatus userStatus);
    
    /**
     * 根据用户ID查询
     */
    CommunityUserStatus selectByUserId(Long userId);
    
    /**
     * 更新用户状态
     */
    int updateById(CommunityUserStatus userStatus);
    
    /**
     * 后台查询用户状态总数
     */
    Long selectAdminUserCount(AdminUserQueryRequest request);
    
    /**
     * 管理端查询用户列表
     */
    List<CommunityUserStatus> selectAdminUserList(@Param("request") AdminUserQueryRequest request);
    
    /**
     * 封禁用户
     */
    int banUser(@Param("userId") Long userId, @Param("reason") String reason, @Param("expireTime") String expireTime);
    
    /**
     * 解封用户
     */
    int unbanUser(Long userId);
    
    /**
     * 更新统计数据
     */
    int updateStatistics(@Param("userId") Long userId, @Param("postCount") Integer postCount, 
                         @Param("commentCount") Integer commentCount, @Param("likeCount") Integer likeCount,
                         @Param("collectCount") Integer collectCount);
    
    /**
     * 增加发帖数
     */
    int incrementPostCount(Long userId);
    
    /**
     * 减少发帖数
     */
    int decrementPostCount(Long userId);

    /**
     * 增加评论数
     */
    int incrementCommentCount(Long userId);
    
    /**
     * 减少评论数
     */
    int decrementCommentCount(Long userId);

    /**
     * 增加点赞数
     */
    int incrementLikeCount(Long userId);
    
    /**
     * 减少点赞数
     */
    int decrementLikeCount(Long userId);
    
    /**
     * 增加收藏数
     */
    int incrementCollectCount(Long userId);
    
    /**
     * 减少收藏数
     */
    int decrementCollectCount(Long userId);
} 