package com.xiaou.bbs.service;

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
     * 根据用户ID获取用户头像
     * @param userId 用户ID
     * @return 用户头像URL
     */
    String getUserAvatarById(String userId);

    /**
     * 批量根据用户ID获取用户信息映射
     * @param userIds 用户ID列表
     * @return 用户ID到用户信息的映射 (包含姓名和头像)
     */
    Map<String, UserInfo> getUserInfosByIds(List<String> userIds);

    /**
     * 用户信息内部类
     */
    class UserInfo {
        private String name;
        private String avatar;

        public UserInfo(String name, String avatar) {
            this.name = name;
            this.avatar = avatar;
        }

        public String getName() { return name; }
        public String getAvatar() { return avatar; }
    }
} 