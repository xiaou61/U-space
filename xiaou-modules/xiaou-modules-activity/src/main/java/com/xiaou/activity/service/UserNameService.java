package com.xiaou.activity.service;

/**
 * 用户名称查询服务
 */
public interface UserNameService {

    /**
     * 根据用户ID获取用户名称
     * @param userId 用户ID
     * @return 用户名称，如果找不到则返回默认格式
     */
    String getUserNameById(String userId);
} 