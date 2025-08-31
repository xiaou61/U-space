package com.xiaou.system.service;

import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.dto.LoginRequest;
import com.xiaou.system.dto.LoginResponse;

import java.util.List;

/**
 * 管理员服务接口
 *
 * @author xiaou
 */
public interface SysAdminService {

    /**
     * 管理员登录
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 根据ID查询管理员
     */
    SysAdmin getById(Long id);

    /**
     * 根据用户名查询管理员
     */
    SysAdmin getByUsername(String username);

    /**
     * 查询管理员列表
     */
    List<SysAdmin> list(SysAdmin admin);

    /**
     * 分页查询管理员列表
     */
    List<SysAdmin> page(SysAdmin admin, Integer pageNum, Integer pageSize);

    /**
     * 新增管理员
     */
    boolean save(SysAdmin admin);

    /**
     * 修改管理员
     */
    boolean update(SysAdmin admin);

    /**
     * 根据ID删除管理员
     */
    boolean removeById(Long id);

    /**
     * 批量删除管理员
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 更新密码
     */
    boolean updatePassword(Long id, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 更新状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username, Long excludeId);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email, Long excludeId);

    /**
     * 获取管理员的角色列表
     */
    List<String> getAdminRoles(Long adminId);

    /**
     * 获取管理员的权限列表
     */
    List<String> getAdminPermissions(Long adminId);
} 