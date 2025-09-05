package com.xiaou.community.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.community.domain.CommunityComment;
import com.xiaou.community.domain.CommunityCommentLike;
import com.xiaou.community.dto.AdminCommentQueryRequest;
import com.xiaou.community.dto.CommunityCommentQueryRequest;
import com.xiaou.community.dto.CommunityCommentCreateRequest;
import com.xiaou.community.dto.CommunityCommentResponse;
import com.xiaou.community.mapper.CommunityCommentMapper;
import com.xiaou.community.mapper.CommunityCommentLikeMapper;
import com.xiaou.community.mapper.CommunityPostMapper;
import com.xiaou.community.service.CommunityCommentService;
import com.xiaou.community.service.CommunityUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 社区评论Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityCommentServiceImpl implements CommunityCommentService {
    
    private final CommunityCommentMapper communityCommentMapper;
    private final CommunityCommentLikeMapper communityCommentLikeMapper;
    private final CommunityPostMapper communityPostMapper;
    private final UserContextUtil userContextUtil;
    private final CommunityUserStatusService communityUserStatusService;
    
    @Override
    public PageResult<CommunityComment> getAdminCommentList(AdminCommentQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> 
            communityCommentMapper.selectAdminCommentList(request)
        );
    }
    
    @Override
    public CommunityComment getById(Long id) {
        CommunityComment comment = communityCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        return comment;
    }
    
    @Override
    public void deleteComment(Long id) {
        CommunityComment comment = getById(id);
        
        int result = communityCommentMapper.deleteComment(id);
        if (result <= 0) {
            throw new BusinessException("删除评论失败");
        }
        
        // 减少用户评论数统计
        communityUserStatusService.decrementCommentCount(comment.getAuthorId());
        
        log.info("评论删除成功，评论ID: {}", id);
    }
    
    @Override
    public List<CommunityComment> getCommentsByUserId(Long userId) {
        return communityCommentMapper.selectByUserId(userId);
    }
    
    // 前台接口实现
    
    @Override
    public PageResult<CommunityCommentResponse> getPostComments(Long postId, CommunityCommentQueryRequest request) {
        // 先获取分页的原始评论数据
        PageResult<CommunityComment> pageResult = PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            return communityCommentMapper.selectPostCommentList(postId, request);
        });
        
        // 在分页外进行DTO转换，避免额外查询干扰分页计数
        List<CommunityCommentResponse> responseList = pageResult.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        // 构造返回结果，保持分页信息
        return PageResult.of(
            pageResult.getPageNum(),
            pageResult.getPageSize(),
            pageResult.getTotal(),
            responseList
        );
    }
    
    @Override
    @Transactional
    public void createComment(Long postId, CommunityCommentCreateRequest request) {
        // 检查用户是否被封禁
        communityUserStatusService.checkUserBanStatus();
        
        UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        
        // 检查帖子是否存在
        if (communityPostMapper.selectById(postId) == null) {
            throw new BusinessException("帖子不存在");
        }
        
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setAuthorId(currentUser.getId());
        comment.setAuthorName(currentUser.getUsername());
        comment.setLikeCount(0);
        comment.setStatus(1);  // 正常状态
        
        int result = communityCommentMapper.insert(comment);
        if (result <= 0) {
            throw new BusinessException("发表评论失败");
        }
        
        // 更新帖子评论数
        communityPostMapper.updateCommentCount(postId, 1);
        
        // 更新用户评论数
        communityUserStatusService.incrementCommentCount(currentUser.getId());
        
        log.info("用户发表评论成功，用户ID: {}, 帖子ID: {}, 评论ID: {}", 
                currentUser.getId(), postId, comment.getId());
    }
    
    @Override
    @Transactional
    public void likeComment(Long commentId) {
        // 检查用户是否被封禁
        communityUserStatusService.checkUserBanStatus();
        
        UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        
        // 检查评论是否存在
        CommunityComment comment = getById(commentId);
        
        // 检查是否已点赞
        CommunityCommentLike existingLike = communityCommentLikeMapper.selectByCommentIdAndUserId(commentId, currentUser.getId());
        if (existingLike != null) {
            throw new BusinessException("已经点赞过了");
        }
        
        // 添加点赞记录
        CommunityCommentLike like = new CommunityCommentLike();
        like.setCommentId(commentId);
        like.setUserId(currentUser.getId());
        like.setUserName(currentUser.getUsername());
        
        int result = communityCommentLikeMapper.insert(like);
        if (result <= 0) {
            throw new BusinessException("点赞失败");
        }
        
        // 更新评论点赞数
        communityCommentMapper.updateLikeCount(commentId, 1);

        // 更新用户点赞数统计
        communityUserStatusService.incrementLikeCount(currentUser.getId());

        log.info("用户点赞评论成功，用户ID: {}, 评论ID: {}", currentUser.getId(), commentId);
    }
    
    @Override
    @Transactional
    public void unlikeComment(Long commentId) {
        UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        
        // 检查评论是否存在
        CommunityComment comment = getById(commentId);
        
        // 检查是否已点赞
        CommunityCommentLike existingLike = communityCommentLikeMapper.selectByCommentIdAndUserId(commentId, currentUser.getId());
        if (existingLike == null) {
            throw new BusinessException("还没有点赞");
        }
        
        // 删除点赞记录
        int result = communityCommentLikeMapper.delete(commentId, currentUser.getId());
        if (result <= 0) {
            throw new BusinessException("取消点赞失败");
        }
        
        // 更新评论点赞数
        communityCommentMapper.updateLikeCount(commentId, -1);

        // 更新用户点赞数统计
        communityUserStatusService.decrementLikeCount(currentUser.getId());

        log.info("用户取消点赞评论成功，用户ID: {}, 评论ID: {}", currentUser.getId(), commentId);
    }
    
    @Override
    public PageResult<CommunityCommentResponse> getUserComments(CommunityCommentQueryRequest request) {
        UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        
        // 先获取分页的原始评论数据
        PageResult<CommunityComment> pageResult = PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            return communityCommentMapper.selectUserCommentList(currentUser.getId(), request);
        });
        
        // 在分页外进行DTO转换，避免额外查询干扰分页计数
        List<CommunityCommentResponse> responseList = pageResult.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        // 构造返回结果，保持分页信息
        return PageResult.of(
            pageResult.getPageNum(),
            pageResult.getPageSize(),
            pageResult.getTotal(),
            responseList
        );
    }
    
    /**
     * 转换为响应DTO
     */
    private CommunityCommentResponse convertToResponse(CommunityComment comment) {
        CommunityCommentResponse response = BeanUtil.copyProperties(comment, CommunityCommentResponse.class);
        
        // 设置用户点赞状态
        UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
        if (currentUser != null) {
            CommunityCommentLike like = communityCommentLikeMapper.selectByCommentIdAndUserId(comment.getId(), currentUser.getId());
            response.setIsLiked(like != null);
        } else {
            response.setIsLiked(false);
        }
        
        return response;
    }
} 