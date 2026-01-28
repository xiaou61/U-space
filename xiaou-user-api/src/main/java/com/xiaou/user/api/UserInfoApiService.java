package com.xiaou.user.api;

import com.xiaou.user.api.dto.SimpleUserInfo;

/**
 * 用户信息API服务接口
 * 供其他模块调用，避免循环依赖
 * 
 * @author xiaou
 */
public interface UserInfoApiService {
    
    /**
     * 根据用户ID获取简单用户信息
     * 
     * @param userId 用户ID
     * @return 简单用户信息，如果用户不存在则返回null
     */
    SimpleUserInfo getSimpleUserInfo(Long userId);
    
    /**
     * 根据用户ID获取显示名称
     * 
     * @param userId 用户ID
     * @return 用户显示名称
     */
    String getUserDisplayName(Long userId);
    
    /**
     * 批量获取简单用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户ID到用户信息的映射
     */
    java.util.Map<Long, SimpleUserInfo> getSimpleUserInfoBatch(java.util.List<Long> userIds);
}
