package com.xiaou.user.dto;

import lombok.Data;

/**
 * 用户查询请求DTO
 *
 * @author xiaou
 */
@Data
public class UserQueryRequest {

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 昵称（模糊查询）
     */
    private String nickname;

    /**
     * 真实姓名（模糊查询）
     */
    private String realName;

    /**
     * 邮箱（模糊查询）
     */
    private String email;

    /**
     * 手机号（模糊查询）
     */
    private String phone;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 状态：0-正常，1-禁用，2-删除
     */
    private Integer status;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
} 