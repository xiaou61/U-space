package com.xiaou.moment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.moment.domain.Moment;
import com.xiaou.moment.domain.MomentComment;
import com.xiaou.moment.domain.MomentLike;
import com.xiaou.moment.dto.*;
import com.xiaou.moment.enums.CommentStatus;
import com.xiaou.moment.enums.MomentStatus;
import com.xiaou.moment.mapper.MomentCommentMapper;
import com.xiaou.moment.mapper.MomentLikeMapper;
import com.xiaou.moment.mapper.MomentMapper;
import com.xiaou.moment.service.MomentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MomentServiceImpl implements MomentService {
    
    private final MomentMapper momentMapper;
    private final MomentLikeMapper momentLikeMapper;
    private final MomentCommentMapper momentCommentMapper;
    private final UserContextUtil userContextUtil;
    
    @Override
    @Transactional
    public Long publishMoment(MomentPublishRequest request) {
                Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        
        // 检查发布频率限制 - 5分钟内最多3条
        checkPublishFrequency(currentUserId);
        
        Moment moment = new Moment();
        moment.setUserId(currentUserId);
        moment.setContent(request.getContent());
        
        // 处理图片URLs
        if (CollUtil.isNotEmpty(request.getImages())) {
            moment.setImages(JSONUtil.toJsonStr(request.getImages()));
        }
        
        moment.setLikeCount(0);
        moment.setCommentCount(0);
        moment.setStatus(MomentStatus.NORMAL.getCode());
        
        momentMapper.insert(moment);
        return moment.getId();
    }
    
    @Override
    public PageResult<MomentListResponse> getMomentList(UserMomentListRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            Map<String, Object> params = new HashMap<>();
            List<Moment> moments = momentMapper.selectList(params);
            
            return moments.stream()
                    .map(this::convertToMomentListResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
        @Transactional
    public void deleteMoment(Long momentId) {
        Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        
        // 只能删除自己的动态
        if (!moment.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权限删除该动态");
        }
        
        // 删除动态（软删除）
        momentMapper.deleteById(momentId);
        
        // 删除相关评论（软删除）
        momentCommentMapper.deleteByMomentId(momentId);
    }
    
    @Override
    @Transactional
    public Boolean toggleLike(Long momentId) {
        Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        
        // 检查动态是否存在
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        
        // 检查是否已点赞
        MomentLike existingLike = momentLikeMapper.selectByMomentIdAndUserId(momentId, currentUserId);
        
        if (existingLike != null) {
            // 取消点赞
            momentLikeMapper.deleteByMomentIdAndUserId(momentId, currentUserId);
            momentMapper.decrementLikeCount(momentId);
            return false;
        } else {
            // 点赞
            MomentLike momentLike = new MomentLike();
            momentLike.setMomentId(momentId);
            momentLike.setUserId(currentUserId);
            momentLikeMapper.insert(momentLike);
            momentMapper.incrementLikeCount(momentId);
            return true;
        }
    }
    
    @Override
    @Transactional
    public Long publishComment(CommentPublishRequest request) {
        Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        
        // 检查动态是否存在
        Moment moment = momentMapper.selectById(request.getMomentId());
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        
        MomentComment comment = new MomentComment();
        comment.setMomentId(request.getMomentId());
        comment.setUserId(currentUserId);
        comment.setContent(request.getContent());
        comment.setStatus(CommentStatus.NORMAL.getCode());
        
        momentCommentMapper.insert(comment);
        
        // 增加评论数
        momentMapper.incrementCommentCount(request.getMomentId());
        
        return comment.getId();
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("请先登录");
        }
        
        MomentComment comment = momentCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 获取动态信息
        Moment moment = momentMapper.selectById(comment.getMomentId());
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        
        // 检查权限：评论者本人或动态发布者可删除
        if (!comment.getUserId().equals(currentUserId) && !moment.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权限删除该评论");
        }
        
        // 删除评论（软删除）
        momentCommentMapper.deleteById(commentId);
        
        // 减少评论数
        momentMapper.decrementCommentCount(comment.getMomentId());
    }
    
    @Override
    public PageResult<AdminMomentListResponse> getAdminMomentList(AdminMomentListRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            Map<String, Object> params = new HashMap<>();
            if (StrUtil.isNotBlank(request.getUserNickname())) {
                params.put("userNickname", request.getUserNickname());
            }
            if (request.getStatus() != null) {
                params.put("status", request.getStatus());
            }
            if (StrUtil.isNotBlank(request.getStartDate())) {
                params.put("startDate", request.getStartDate());
            }
            if (StrUtil.isNotBlank(request.getEndDate())) {
                params.put("endDate", request.getEndDate());
            }
            if (request.getHasSensitiveWord() != null) {
                // 敏感词功能暂未实现，这里预留参数
                params.put("hasSensitiveWord", request.getHasSensitiveWord());
            }
            
            List<Moment> moments = momentMapper.selectList(params);
            
            return moments.stream()
                    .map(this::convertToAdminMomentListResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    @Transactional
    public void batchDeleteMoments(List<Long> momentIds) {
        if (CollUtil.isEmpty(momentIds)) {
            return;
        }
        
        for (Long momentId : momentIds) {
            momentMapper.deleteById(momentId);
            momentCommentMapper.deleteByMomentId(momentId);
        }
    }
    
    /**
     * 转换为动态列表响应
     */
    private MomentListResponse convertToMomentListResponse(Moment moment) {
        MomentListResponse response = BeanUtil.copyProperties(moment, MomentListResponse.class);
        
        // 处理图片URLs
        if (StrUtil.isNotBlank(moment.getImages())) {
            response.setImages(JSONUtil.toList(moment.getImages(), String.class));
        } else {
            response.setImages(new ArrayList<>());
        }
        
        // 设置用户信息（这里需要查询用户表，暂时使用占位符）
        response.setUserNickname("用户" + moment.getUserId());
        response.setUserAvatar("");
        
        // 设置是否已点赞
        Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId != null) {
            MomentLike like = momentLikeMapper.selectByMomentIdAndUserId(moment.getId(), currentUserId);
            response.setIsLiked(like != null);
            response.setCanDelete(moment.getUserId().equals(currentUserId));
        } else {
            response.setIsLiked(false);
            response.setCanDelete(false);
        }
        
        // 获取最新3条评论
        List<MomentComment> comments = momentCommentMapper.selectByMomentId(moment.getId(), 3);
        List<CommentResponse> commentResponses = comments.stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
        response.setRecentComments(commentResponses);
        
        return response;
    }
    
    /**
     * 转换为管理端动态列表响应
     */
    private AdminMomentListResponse convertToAdminMomentListResponse(Moment moment) {
        AdminMomentListResponse response = BeanUtil.copyProperties(moment, AdminMomentListResponse.class);
        
        // 处理图片URLs
        if (StrUtil.isNotBlank(moment.getImages())) {
            response.setImages(JSONUtil.toList(moment.getImages(), String.class));
        } else {
            response.setImages(new ArrayList<>());
        }
        
        // 设置状态描述
        if (moment.getStatus().equals(MomentStatus.NORMAL.getCode())) {
            response.setStatusDesc(MomentStatus.NORMAL.getDesc());
        } else if (moment.getStatus().equals(MomentStatus.DELETED.getCode())) {
            response.setStatusDesc(MomentStatus.DELETED.getDesc());
        } else if (moment.getStatus().equals(MomentStatus.REVIEWING.getCode())) {
            response.setStatusDesc(MomentStatus.REVIEWING.getDesc());
        }
        
        // 设置用户信息
        response.setUserNickname("用户" + moment.getUserId());
        
        // 检查敏感词（暂时设为false，后续可接入敏感词检测服务）
        response.setHasSensitiveWord(false);
        
        return response;
    }
    
    /**
     * 转换为评论响应
     */
    private CommentResponse convertToCommentResponse(MomentComment comment) {
        CommentResponse response = BeanUtil.copyProperties(comment, CommentResponse.class);
        
        // 设置用户信息
        response.setUserNickname("用户" + comment.getUserId());
        response.setUserAvatar("");
        
        // 设置是否可删除
        Long currentUserId = userContextUtil.getCurrentUserId();
        if (currentUserId != null) {
            // 评论者本人或动态发布者可删除
            Moment moment = momentMapper.selectById(comment.getMomentId());
            response.setCanDelete(comment.getUserId().equals(currentUserId) || 
                                (moment != null && moment.getUserId().equals(currentUserId)));
        } else {
            response.setCanDelete(false);
        }
        
        return response;
    }
    
    /**
     * 检查发布频率限制
     */
    private void checkPublishFrequency(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("startDate", LocalDateTime.now().minusMinutes(5));
        
        Long count = momentMapper.selectCount(params);
        if (count >= 3) {
            throw new BusinessException("发布过于频繁，请稍后再试");
        }
    }
    
    @Override
    public PageResult<AdminCommentListResponse> getAdminCommentList(AdminCommentListRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            Map<String, Object> params = new HashMap<>();
            if (request.getMomentId() != null) {
                params.put("momentId", request.getMomentId());
            }
            if (StrUtil.isNotBlank(request.getUserNickname())) {
                params.put("userNickname", request.getUserNickname());
            }
            if (StrUtil.isNotBlank(request.getContentKeyword())) {
                params.put("contentKeyword", "%" + request.getContentKeyword() + "%");
            }
            if (request.getStatus() != null) {
                params.put("status", request.getStatus());
            }
            if (StrUtil.isNotBlank(request.getStartDate())) {
                params.put("startDate", request.getStartDate());
            }
            if (StrUtil.isNotBlank(request.getEndDate())) {
                params.put("endDate", request.getEndDate());
            }
            
            List<MomentComment> comments = momentCommentMapper.selectList(params);
            
            return comments.stream()
                    .map(this::convertToAdminCommentListResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    @Transactional
    public void adminDeleteComment(Long commentId) {
        MomentComment comment = momentCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 删除评论（软删除）
        momentCommentMapper.deleteById(commentId);
        
        // 减少评论数
        momentMapper.decrementCommentCount(comment.getMomentId());
    }
    
    @Override
    public PageResult<CommentResponse> getMomentComments(UserMomentCommentsRequest request) {
        // 检查动态是否存在
        Moment moment = momentMapper.selectById(request.getMomentId());
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            List<MomentComment> comments = momentCommentMapper.selectByMomentId(request.getMomentId(), null);
            
            return comments.stream()
                    .map(this::convertToCommentResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public MomentStatisticsResponse getStatistics(StatisticsRequest request) {
        MomentStatisticsResponse response = new MomentStatisticsResponse();
        
        // 统计总数据
        Map<String, Object> params = new HashMap<>();
        if (StrUtil.isNotBlank(request.getStartDate())) {
            params.put("startDate", request.getStartDate());
        }
        if (StrUtil.isNotBlank(request.getEndDate())) {
            params.put("endDate", request.getEndDate());
        }
        
        // 总动态数
        response.setTotalMoments(momentMapper.selectCount(params));
        
        // 总点赞数（需要新增方法统计）
        response.setTotalLikes(getTotalLikes(params));
        
        // 总评论数
        response.setTotalComments(momentCommentMapper.selectCount(params));
        
        // 活跃用户数（发布过动态的用户数）
        response.setActiveUsers(getActiveUsersCount(params));
        
        // 每日统计数据
        response.setDailyStats(getDailyStatistics(request));
        
        return response;
    }
    
    /**
     * 转换为管理端评论列表响应
     */
    private AdminCommentListResponse convertToAdminCommentListResponse(MomentComment comment) {
        AdminCommentListResponse response = BeanUtil.copyProperties(comment, AdminCommentListResponse.class);
        
        // 设置状态描述
        if (comment.getStatus().equals(CommentStatus.NORMAL.getCode())) {
            response.setStatusDesc(CommentStatus.NORMAL.getDesc());
        } else if (comment.getStatus().equals(CommentStatus.DELETED.getCode())) {
            response.setStatusDesc(CommentStatus.DELETED.getDesc());
        }
        
        // 设置用户信息
        response.setUserNickname("用户" + comment.getUserId());
        
        // 设置动态内容摘要
        Moment moment = momentMapper.selectById(comment.getMomentId());
        if (moment != null) {
            String content = moment.getContent();
            if (StrUtil.length(content) > 50) {
                content = StrUtil.sub(content, 0, 50) + "...";
            }
            response.setMomentContentSummary(content);
        }
        
        return response;
    }
    
    /**
     * 获取总点赞数
     */
    private Long getTotalLikes(Map<String, Object> params) {
        return momentMapper.selectTotalLikes(params);
    }
    
    /**
     * 获取活跃用户数
     */
    private Long getActiveUsersCount(Map<String, Object> params) {
        return momentMapper.selectActiveUsersCount(params);
    }
    
    /**
     * 获取每日统计数据
     */
    private List<DailyStatistics> getDailyStatistics(StatisticsRequest request) {
        List<DailyStatistics> dailyStats = new ArrayList<>();
        
        // 如果没有指定日期范围，默认返回最近7天的数据
        String endDate = StrUtil.isNotBlank(request.getEndDate()) ? request.getEndDate() : 
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String startDate = StrUtil.isNotBlank(request.getStartDate()) ? request.getStartDate() : 
                          LocalDateTime.now().minusDays(6).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 按天循环统计
            LocalDateTime current = start;
            while (!current.isAfter(end)) {
                String dateStr = current.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 统计当天的数据
                Map<String, Object> dayParams = new HashMap<>();
                dayParams.put("startDate", dateStr + " 00:00:00");
                dayParams.put("endDate", dateStr + " 23:59:59");
                
                DailyStatistics dayStat = new DailyStatistics();
                dayStat.setDate(dateStr);
                dayStat.setMomentCount(momentMapper.selectCount(dayParams));
                dayStat.setLikeCount(momentMapper.selectTotalLikes(dayParams));
                dayStat.setCommentCount(momentCommentMapper.selectCount(dayParams));
                dayStat.setActiveUserCount(momentMapper.selectActiveUsersCount(dayParams));
                
                dailyStats.add(dayStat);
                current = current.plusDays(1);
            }
        } catch (Exception e) {
            log.error("获取每日统计数据失败", e);
        }
        
        return dailyStats;
    }
} 