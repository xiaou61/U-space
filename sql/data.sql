

USE `code_nest`;

-- 1. 初始化角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `sort_order`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 0, 1),
(2, '系统管理员', 'SYSTEM_ADMIN', '系统管理员，负责系统配置和用户管理', 0, 2),
(3, '普通管理员', 'ADMIN', '普通管理员，负责日常业务管理', 0, 3);

-- 2. 初始化管理员用户数据
-- 默认密码：123456 (BCrypt加密后的值)
INSERT INTO `sys_admin` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `gender`, `status`, `remark`, `create_by`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSIOUQEb8tlCe5bwvJQpJk5E3KtZLOhJQBFgF8LDfN.6', '超级管理员', 'admin@code-nest.com', '13800138000', 1, 0, '系统内置超级管理员账户', 1),
(2, 'system', '$2a$10$7JB720yubVSIOUQEb8tlCe5bwvJQpJk5E3KtZLOhJQBFgF8LDfN.6', '系统管理员', 'system@code-nest.com', '13800138001', 1, 0, '系统内置系统管理员账户', 1);

-- 3. 初始化管理员角色关联
INSERT INTO `sys_admin_role` (`admin_id`, `role_id`, `create_by`) VALUES
(1, 1, 1),  -- admin 用户分配超级管理员角色
(2, 2, 1);  -- system 用户分配系统管理员角色

-- 4. 初始化权限数据
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `component`, `icon`, `sort_order`, `status`, `description`) VALUES
-- 一级菜单
(1, 0, '系统管理', 'system', 0, '/system', '', 'system', 1, 0, '系统管理模块'),
(2, 0, '用户管理', 'user', 0, '/user', '', 'user', 2, 0, '用户管理模块'),
(3, 0, '内容管理', 'content', 0, '/content', '', 'document', 3, 0, '内容管理模块'),

-- 系统管理子菜单
(11, 1, '管理员管理', 'system:admin', 0, '/system/admin', 'system/admin/index', 'user-tie', 1, 0, '管理员用户管理'),
(12, 1, '角色管理', 'system:role', 0, '/system/role', 'system/role/index', 'peoples', 2, 0, '角色权限管理'),
(13, 1, '权限管理', 'system:permission', 0, '/system/permission', 'system/permission/index', 'tree', 3, 0, '权限菜单管理'),
(14, 1, '登录日志', 'system:login-log', 0, '/system/login-log', 'system/login-log/index', 'documentation', 4, 0, '登录日志查看'),

-- 管理员管理按钮权限
(111, 11, '管理员查询', 'system:admin:query', 1, '', '', '', 1, 0, '管理员列表查询权限'),
(112, 11, '管理员新增', 'system:admin:add', 1, '', '', '', 2, 0, '管理员新增权限'),
(113, 11, '管理员修改', 'system:admin:edit', 1, '', '', '', 3, 0, '管理员修改权限'),
(114, 11, '管理员删除', 'system:admin:delete', 1, '', '', '', 4, 0, '管理员删除权限'),
(115, 11, '重置密码', 'system:admin:reset-pwd', 1, '', '', '', 5, 0, '重置管理员密码权限'),

-- 角色管理按钮权限
(121, 12, '角色查询', 'system:role:query', 1, '', '', '', 1, 0, '角色列表查询权限'),
(122, 12, '角色新增', 'system:role:add', 1, '', '', '', 2, 0, '角色新增权限'),
(123, 12, '角色修改', 'system:role:edit', 1, '', '', '', 3, 0, '角色修改权限'),
(124, 12, '角色删除', 'system:role:delete', 1, '', '', '', 4, 0, '角色删除权限'),
(125, 12, '分配权限', 'system:role:assign-permission', 1, '', '', '', 5, 0, '角色分配权限权限'),

-- 权限管理按钮权限
(131, 13, '权限查询', 'system:permission:query', 1, '', '', '', 1, 0, '权限列表查询权限'),
(132, 13, '权限新增', 'system:permission:add', 1, '', '', '', 2, 0, '权限新增权限'),
(133, 13, '权限修改', 'system:permission:edit', 1, '', '', '', 3, 0, '权限修改权限'),
(134, 13, '权限删除', 'system:permission:delete', 1, '', '', '', 4, 0, '权限删除权限');

-- 5. 初始化角色权限关联（超级管理员拥有所有权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_by`)
SELECT 1, `id`, 1 FROM `sys_permission` WHERE `status` = 0;

-- 6. 初始化系统管理员权限（除了权限管理的删除权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_by`)
SELECT 2, `id`, 1 FROM `sys_permission` 
WHERE `status` = 0 AND `id` NOT IN (134);  -- 排除权限删除权限 