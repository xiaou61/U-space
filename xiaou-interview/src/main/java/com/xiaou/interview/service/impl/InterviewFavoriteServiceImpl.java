package com.xiaou.interview.service.impl;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.interview.domain.InterviewFavorite;
import com.xiaou.interview.mapper.InterviewFavoriteMapper;
import com.xiaou.interview.mapper.InterviewQuestionMapper;
import com.xiaou.interview.mapper.InterviewQuestionSetMapper;
import com.xiaou.interview.service.InterviewFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 面试收藏服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewFavoriteServiceImpl implements InterviewFavoriteService {

    private final InterviewFavoriteMapper favoriteMapper;
    private final InterviewQuestionMapper questionMapper;
    private final InterviewQuestionSetMapper questionSetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Integer targetType, Long targetId) {
        // 验证收藏目标是否存在
        validateTarget(targetType, targetId);

        // 检查是否已收藏
        if (favoriteMapper.exists(userId, targetType, targetId)) {
            throw new BusinessException("已经收藏过了");
        }

        InterviewFavorite favorite = new InterviewFavorite()
                .setUserId(userId)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setCreateTime(LocalDateTime.now());

        int result = favoriteMapper.insert(favorite);
        if (result <= 0) {
            throw new BusinessException("收藏失败");
        }

        // 增加目标的收藏次数
        if (targetType == 1) { // 题单
            questionSetMapper.increaseFavoriteCount(targetId);
        } else if (targetType == 2) { // 题目
            questionMapper.increaseFavoriteCount(targetId);
        }

        log.info("收藏成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Integer targetType, Long targetId) {
        // 检查收藏是否存在
        if (!favoriteMapper.exists(userId, targetType, targetId)) {
            throw new BusinessException("收藏记录不存在");
        }

        int result = favoriteMapper.delete(userId, targetType, targetId);
        if (result <= 0) {
            throw new BusinessException("取消收藏失败");
        }

        // 减少目标的收藏次数
        if (targetType == 1) { // 题单
            questionSetMapper.decreaseFavoriteCount(targetId);
        } else if (targetType == 2) { // 题目
            questionMapper.decreaseFavoriteCount(targetId);
        }

        log.info("取消收藏成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
    }

    @Override
    public boolean isFavorited(Long userId, Integer targetType, Long targetId) {
        return favoriteMapper.exists(userId, targetType, targetId);
    }

    @Override
    public List<InterviewFavorite> getUserFavorites(Long userId, Integer targetType) {
        if (targetType == 1) { // 题单
            return favoriteMapper.selectQuestionSetsByUserId(userId, 0, Integer.MAX_VALUE);
        } else if (targetType == 2) { // 题目
            return favoriteMapper.selectQuestionsByUserId(userId, 0, Integer.MAX_VALUE);
        }
        return Collections.emptyList();
    }

    @Override
    public PageResult<InterviewFavorite> getUserFavoritePage(Long userId, Integer targetType, int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        List<InterviewFavorite> favorites;
        long total;
        
        if (targetType == 1) { // 题单
            favorites = favoriteMapper.selectQuestionSetsByUserId(userId, offset, size);
            total = favoriteMapper.countQuestionSetsByUserId(userId);
        } else if (targetType == 2) { // 题目
            favorites = favoriteMapper.selectQuestionsByUserId(userId, offset, size);
            total = favoriteMapper.countQuestionsByUserId(userId);
        } else {
            favorites = Collections.emptyList();
            total = 0;
        }

        return PageResult.of(page, size, total, favorites);
    }

    @Override
    public int getFavoriteCount(Integer targetType, Long targetId) {
        return favoriteMapper.countByTarget(targetType, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveFavorites(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除收藏：传入的ID列表为空");
            return;
        }

        try {
            int deletedCount = favoriteMapper.batchDeleteByIds(ids);
            log.info("批量删除收藏成功，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("批量删除收藏失败", e);
            throw new BusinessException("批量删除收藏失败");
        }
    }

    /**
     * 验证收藏目标是否存在
     */
    private void validateTarget(Integer targetType, Long targetId) {
        if (targetType == 1) { // 题单
            if (questionSetMapper.selectById(targetId) == null) {
                throw new BusinessException("题单不存在");
            }
        } else if (targetType == 2) { // 题目
            if (questionMapper.selectById(targetId) == null) {
                throw new BusinessException("题目不存在");
            }
        } else {
            throw new BusinessException("不支持的收藏类型");
        }
    }
} 