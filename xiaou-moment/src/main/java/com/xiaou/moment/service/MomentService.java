package com.xiaou.moment.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.moment.dto.*;

import java.util.List;

/**
 * 动态Service接口
 */
public interface MomentService {
    
    /**
     * 发布动态
     */
    Long publishMoment(MomentPublishRequest request);
    
    /**
     * 获取动态列表
     */
    PageResult<MomentListResponse> getMomentList(UserMomentListRequest request);
    
    /**
     * 删除动态
     */
    void deleteMoment(Long momentId);
    
    /**
     * 点赞/取消点赞
     */
    Boolean toggleLike(Long momentId);
    
    /**
     * 发布评论
     */
    Long publishComment(CommentPublishRequest request);
    
    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
    
    /**
     * 获取管理端动态列表
     */
    PageResult<AdminMomentListResponse> getAdminMomentList(AdminMomentListRequest request);
    
    /**
     * 批量删除动态
     */
    void batchDeleteMoments(List<Long> momentIds);
    
    /**
     * 获取管理端评论列表
     */
    PageResult<AdminCommentListResponse> getAdminCommentList(AdminCommentListRequest request);
    
    /**
     * 管理端删除评论
     */
    void adminDeleteComment(Long commentId);
    
    /**
     * 获取动态的完整评论列表
     */
    PageResult<CommentResponse> getMomentComments(UserMomentCommentsRequest request);
    
    /**
     * 获取统计数据
     */
    MomentStatisticsResponse getStatistics(StatisticsRequest request);
    
    /**
     * 获取热门动态
     */
    List<MomentListResponse> getHotMoments(HotMomentRequest request);
    
    /**
     * 搜索动态
     */
    PageResult<MomentListResponse> searchMoments(MomentSearchRequest request);
    
    /**
     * 获取用户个人动态列表
     */
    PageResult<MomentListResponse> getUserMomentList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取用户动态信息
     */
    UserMomentInfoResponse getUserMomentInfo(Long userId);
    
    /**
     * 收藏/取消收藏
     */
    Boolean toggleFavorite(Long momentId);
    
    /**
     * 获取我的收藏列表
     */
    PageResult<MomentListResponse> getMyFavorites(Integer pageNum, Integer pageSize);
} 