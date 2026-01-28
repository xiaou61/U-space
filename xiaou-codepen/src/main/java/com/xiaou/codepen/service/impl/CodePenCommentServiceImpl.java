package com.xiaou.codepen.service.impl;

import com.xiaou.codepen.domain.CodePenComment;
import com.xiaou.codepen.dto.CommentCreateRequest;
import com.xiaou.codepen.mapper.CodePenCommentMapper;
import com.xiaou.codepen.mapper.CodePenMapper;
import com.xiaou.codepen.service.CodePenCommentService;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.user.api.UserInfoApiService;
import com.xiaou.user.api.dto.SimpleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 作品评论Service实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodePenCommentServiceImpl implements CodePenCommentService {
    
    private final CodePenCommentMapper commentMapper;
    private final CodePenMapper codePenMapper;
    private final UserInfoApiService userInfoApiService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentCreateRequest request, Long userId) {
        // 验证内容
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (request.getContent().length() > 500) {
            throw new BusinessException("评论内容不能超过500个字符");
        }
        
        CodePenComment comment = new CodePenComment();
        comment.setPenId(request.getPenId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setStatus(1);
        
        commentMapper.insert(comment);
        
        // 增加作品评论数
        codePenMapper.incrementCommentCount(request.getPenId());
        
        log.info("用户{}为作品{}发表评论成功", userId, request.getPenId());
        
        return comment.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id, Long userId) {
        CodePenComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        commentMapper.deleteById(id);
        codePenMapper.decrementCommentCount(comment.getPenId());
        
        return true;
    }
    
    @Override
    public List<CodePenComment> getCommentList(Long penId) {
        List<CodePenComment> comments = commentMapper.selectByPenId(penId, 1);
        
        if (comments.isEmpty()) {
            return comments;
        }
        
        // 批量查询用户信息，避免N+1问题
        java.util.List<Long> userIds = comments.stream()
                .map(CodePenComment::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        java.util.Map<Long, SimpleUserInfo> userInfoMap = userInfoApiService.getSimpleUserInfoBatch(userIds);
        
        // 填充用户信息
        for (CodePenComment comment : comments) {
            if (comment.getUserId() != null) {
                SimpleUserInfo userInfo = userInfoMap.get(comment.getUserId());
                if (userInfo != null) {
                    comment.setUserNickname(userInfo.getDisplayName());
                    comment.setUserAvatar(userInfo.getAvatar());
                }
            }
        }
        
        return comments;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean hideComment(Long id) {
        CodePenComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 更新评论状态为隐藏（2）
        commentMapper.updateStatus(id, 2);
        
        log.info("管理员隐藏评论成功，评论ID：{}", id);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminDeleteComment(Long id) {
        CodePenComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 删除评论
        commentMapper.deleteById(id);
        
        // 减少作品评论数
        codePenMapper.decrementCommentCount(comment.getPenId());
        
        log.info("管理员删除评论成功，评论ID：{}", id);
        
        return true;
    }
}

