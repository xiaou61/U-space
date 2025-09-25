package com.xiaou.user.service.impl;

import com.xiaou.user.api.UserInfoApiService;
import com.xiaou.user.api.dto.SimpleUserInfo;
import com.xiaou.user.domain.UserInfo;
import com.xiaou.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户信息API服务实现类
 * 供其他模块调用，避免循环依赖
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoApiServiceImpl implements UserInfoApiService {
    
    private final UserInfoMapper userInfoMapper;
    
    @Override
    public SimpleUserInfo getSimpleUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            UserInfo user = userInfoMapper.selectById(userId);
            if (user == null || user.getStatus() == 2) {
                return null;
            }
            
            return SimpleUserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .realName(user.getRealName())
                    .avatar(user.getAvatar())
                    .build();
                    
        } catch (Exception e) {
            log.warn("获取用户信息失败，userId: {}, 错误: {}", userId, e.getMessage());
            return null;
        }
    }
    
    @Override
    public String getUserDisplayName(Long userId) {
        SimpleUserInfo userInfo = getSimpleUserInfo(userId);
        return userInfo != null ? userInfo.getDisplayName() : "用户" + userId;
    }
}
