package com.xiaou.community.service.impl;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.satoken.SaTokenUserUtil;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.common.utils.DateHelper;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.dto.AdminUserQueryRequest;
import com.xiaou.community.mapper.CommunityUserStatusMapper;
import com.xiaou.community.service.CommunityUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户社区状态Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityUserStatusServiceImpl implements CommunityUserStatusService {
    
    private final CommunityUserStatusMapper communityUserStatusMapper;
    
    @Override
    public PageResult<CommunityUserStatus> getAdminUserList(AdminUserQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> 
            communityUserStatusMapper.selectAdminUserList(request)
        );
    }
    
    @Override
    public CommunityUserStatus getByUserId(Long userId) {
        return communityUserStatusMapper.selectByUserId(userId);
    }
    
    @Override
    public void banUser(Long userId, String reason, Integer duration) {
        // 计算封禁过期时间
        Date expireTime = DateHelper.addHoursFromNow(duration);
        String expireTimeStr = DateHelper.formatDateTime(expireTime);
        
        int result = communityUserStatusMapper.banUser(userId, reason, expireTimeStr);
        if (result <= 0) {
            throw new BusinessException("封禁用户失败");
        }
        
        log.info("用户封禁成功，用户ID: {}, 封禁原因: {}, 封禁时长: {}小时", userId, reason, duration);
    }
    
    @Override
    public void unbanUser(Long userId) {
        int result = communityUserStatusMapper.unbanUser(userId);
        if (result <= 0) {
            throw new BusinessException("解封用户失败");
        }
        
        log.info("用户解封成功，用户ID: {}", userId);
    }
    
    @Override
    public CommunityUserStatus ensureUserExists(Long userId, String userName) {
        // 检查用户是否已存在
        CommunityUserStatus existingUser = communityUserStatusMapper.selectByUserId(userId);
        if (existingUser != null) {
            return existingUser;
        }
        
        // 用户不存在，创建新记录
        CommunityUserStatus newUser = new CommunityUserStatus();
        newUser.setUserId(userId);
        newUser.setUserName(userName);
        newUser.setPostCount(0);
        newUser.setCommentCount(0);
        newUser.setLikeCount(0);
        newUser.setCollectCount(0);
        newUser.setIsBanned(0);
        newUser.setBanReason(null);
        newUser.setBanExpireTime(null);
        newUser.setCreateTime(DateHelper.now());
        newUser.setUpdateTime(DateHelper.now());
        
        int result = communityUserStatusMapper.insert(newUser);
        if (result <= 0) {
            throw new BusinessException("创建用户社区记录失败");
        }
        
        log.info("用户社区记录创建成功，用户ID: {}, 用户名: {}", userId, userName);
        return newUser;
    }
    
    @Override
    public CommunityUserStatus getCurrentUserStatus() {
        if (!StpUserUtil.isLogin()) {
            throw new BusinessException("请先登录");
        }
        Long currentUserId = StpUserUtil.getLoginIdAsLong();
        
        // Sa-Token: 从工具类获取用户名
        String username = SaTokenUserUtil.getCurrentUserUsername("用户" + currentUserId);
        
        return ensureUserExists(currentUserId, username);
    }
    
    @Override
    public void checkUserBanStatus() {
        if (!StpUserUtil.isLogin()) {
            throw new BusinessException("请先登录");
        }
        Long currentUserId = StpUserUtil.getLoginIdAsLong();
        
        CommunityUserStatus userStatus = getByUserId(currentUserId);
        if (userStatus == null) {
            // 用户不存在则自动创建
            // Sa-Token: 从工具类获取用户名
            String username = SaTokenUserUtil.getCurrentUserUsername("用户" + currentUserId);
            ensureUserExists(currentUserId, username);
            return;
        }
        
        // 检查是否被封禁
        if (userStatus.getIsBanned() != null && userStatus.getIsBanned() == 1) {
            // 检查封禁是否过期
            if (userStatus.getBanExpireTime() != null && userStatus.getBanExpireTime().before(new Date())) {
                // 封禁已过期，自动解封
                unbanUser(currentUserId);
                return;
            }
            
            String reason = userStatus.getBanReason() != null ? userStatus.getBanReason() : "违规操作";
            throw new BusinessException("您已被封禁，原因：" + reason);
        }
    }
    
    @Override
    public void incrementPostCount(Long userId) {
        int result = communityUserStatusMapper.incrementPostCount(userId);
        if (result <= 0) {
            log.warn("更新用户发帖数失败，用户ID: {}", userId);
        }
    }
    
    @Override
    public void decrementPostCount(Long userId) {
        int result = communityUserStatusMapper.decrementPostCount(userId);
        if (result <= 0) {
            log.warn("减少用户发帖数失败，用户ID: {}", userId);
        }
    }

    @Override
    public void incrementCommentCount(Long userId) {
        int result = communityUserStatusMapper.incrementCommentCount(userId);
        if (result <= 0) {
            log.warn("更新用户评论数失败，用户ID: {}", userId);
        }
    }
    
    @Override
    public void decrementCommentCount(Long userId) {
        int result = communityUserStatusMapper.decrementCommentCount(userId);
        if (result <= 0) {
            log.warn("减少用户评论数失败，用户ID: {}", userId);
        }
    }
    
    @Override
    public void incrementLikeCount(Long userId) {
        int result = communityUserStatusMapper.incrementLikeCount(userId);
        if (result <= 0) {
            log.warn("更新用户点赞数失败，用户ID: {}", userId);
        }
    }
    
    @Override
    public void decrementLikeCount(Long userId) {
        int result = communityUserStatusMapper.decrementLikeCount(userId);
        if (result <= 0) {
            log.warn("减少用户点赞数失败，用户ID: {}", userId);
        }
    }

    @Override
    public void incrementCollectCount(Long userId) {
        int result = communityUserStatusMapper.incrementCollectCount(userId);
        if (result <= 0) {
            log.warn("更新用户收藏数失败，用户ID: {}", userId);
        }
    }
    
    @Override
    public void decrementCollectCount(Long userId) {
        int result = communityUserStatusMapper.decrementCollectCount(userId);
        if (result <= 0) {
            log.warn("减少用户收藏数失败，用户ID: {}", userId);
        }
    }
    
    @Override
    public CommunityUserStatus getUserStatusByUserId(Long userId) {
        CommunityUserStatus userStatus = communityUserStatusMapper.selectByUserId(userId);
        if (userStatus == null) {
            // 用户不存在，创建默认记录
            String username = SaTokenUserUtil.getUsernameById(userId, "用户" + userId);
            return ensureUserExists(userId, username);
        }
        return userStatus;
    }
} 