package com.xiaou.auth.admin.domain.resp;

import lombok.Data;

import java.util.List;

/**
 * 管理员用户及角色信息响应类
 * 
 * @author xiaou
 */
@Data
public class AdminUserWithRolesResp {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色列表（逗号分隔的字符串）
     */
    private String roles;
} 