package com.xiaou.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.common.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 提供获取当前登录用户信息的统一方法
 *
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserContextUtil {

    private final JwtTokenUtil jwtTokenUtil;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    /**
     * 用户信息DTO
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String avatar;
        private Integer gender;
        private Integer status;
        private String userType; // admin 或 user

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public Integer getGender() {
            return gender;
        }

        public void setGender(Integer gender) {
            this.gender = gender;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        /**
         * 是否为管理员
         */
        public boolean isAdmin() {
            return "admin".equals(userType);
        }

        /**
         * 是否为普通用户
         */
        public boolean isUser() {
            return "user".equals(userType);
        }
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息，如果未登录或token无效则返回null
     */
    public UserInfo getCurrentUser() {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.debug("无法获取当前请求上下文");
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("请求头中未找到有效的Authorization信息");
                return null;
            }

            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token == null) {
                log.debug("无法从请求头中解析Token");
                return null;
            }

            // 验证token
            if (!jwtTokenUtil.validateToken(token)) {
                log.debug("Token验证失败");
                return null;
            }

            // 获取用户类型
            String userType = jwtTokenUtil.getUserTypeFromToken(token);
            if (userType == null) {
                log.debug("无法从Token中获取用户类型");
                return null;
            }

            // 根据用户类型获取用户信息
            String userInfoJson;
            if ("admin".equals(userType)) {
                userInfoJson = tokenService.getAdminFromToken(token);
            } else if ("user".equals(userType)) {
                userInfoJson = tokenService.getUserFromToken(token, "user");
            } else {
                log.debug("未知的用户类型: {}", userType);
                return null;
            }

            if (userInfoJson == null) {
                log.debug("无法从Redis中获取用户信息，用户类型: {}", userType);
                return null;
            }

            // 将JSON转换为UserInfo对象
            UserInfo userInfo = convertToUserInfo(userInfoJson, userType);
            
            log.debug("成功获取当前用户信息: {}({})", userInfo.getUsername(), userType);
            return userInfo;

        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID，如果未登录或token无效则返回null
     */
    public Long getCurrentUserId() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getId() : null;
    }

    /**
     * 获取当前登录用户名
     * 
     * @return 用户名，如果未登录或token无效则返回null
     */
    public String getCurrentUsername() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    /**
     * 检查当前用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isCurrentUserAdmin() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null && userInfo.isAdmin();
    }

    /**
     * 检查当前用户是否为普通用户
     * 
     * @return 是否为普通用户
     */
    public boolean isCurrentUserRegular() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null && userInfo.isUser();
    }

    /**
     * 将JSON字符串转换为UserInfo对象
     * 
     * @param userInfoJson JSON字符串
     * @param userType 用户类型
     * @return UserInfo对象
     */
    private UserInfo convertToUserInfo(String userInfoJson, String userType) {
        try {
            if ("admin".equals(userType)) {
                // 管理员类型，解析为SysAdmin字段
                var adminNode = objectMapper.readTree(userInfoJson);
                UserInfo userInfo = new UserInfo();
                userInfo.setId(adminNode.get("id").asLong());
                userInfo.setUsername(getStringValue(adminNode, "username"));
                userInfo.setRealName(getStringValue(adminNode, "realName"));
                userInfo.setEmail(getStringValue(adminNode, "email"));
                userInfo.setPhone(getStringValue(adminNode, "phone"));
                userInfo.setAvatar(getStringValue(adminNode, "avatar"));
                userInfo.setGender(getIntValue(adminNode, "gender"));
                userInfo.setStatus(getIntValue(adminNode, "status"));
                userInfo.setUserType("admin");
                return userInfo;
            } else {
                // 普通用户类型，解析为UserInfo字段
                var userNode = objectMapper.readTree(userInfoJson);
                UserInfo userInfo = new UserInfo();
                userInfo.setId(userNode.get("id").asLong());
                userInfo.setUsername(getStringValue(userNode, "username"));
                userInfo.setRealName(getStringValue(userNode, "realName"));
                userInfo.setEmail(getStringValue(userNode, "email"));
                userInfo.setPhone(getStringValue(userNode, "phone"));
                userInfo.setAvatar(getStringValue(userNode, "avatar"));
                userInfo.setGender(getIntValue(userNode, "gender"));
                userInfo.setStatus(getIntValue(userNode, "status"));
                userInfo.setUserType("user");
                return userInfo;
            }
        } catch (Exception e) {
            log.error("转换用户信息JSON失败", e);
            throw new RuntimeException("用户信息解析失败", e);
        }
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        var fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    /**
     * 安全获取整数值
     */
    private Integer getIntValue(com.fasterxml.jackson.databind.JsonNode node, String fieldName) {
        var fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asInt() : null;
    }
} 