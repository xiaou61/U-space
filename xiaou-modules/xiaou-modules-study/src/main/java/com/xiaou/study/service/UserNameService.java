package com.xiaou.study.service;

import java.util.List;
import java.util.Map;

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

    /**
     * 批量根据用户ID获取用户名称映射
     * @param userIds 用户ID列表
     * @return 用户ID到姓名的映射
     */
    Map<String, String> getUserNamesByIds(List<String> userIds);
} 