-- 角色权限管理系统数据库表结构 v2.3.1

-- 系统角色表
CREATE TABLE `sys_role` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '角色ID，主键UUID',
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码，如：admin、teacher、bbs_admin',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称，如：系统管理员、教师、BBS管理员',
    `description` TEXT COMMENT '角色描述',
    `is_system` TINYINT(1) DEFAULT 0 COMMENT '是否系统内置角色：1是 0否（系统角色不能删除）',
    `sort_order` INT DEFAULT 0 COMMENT '排序字段',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_by` VARCHAR(32) COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) COMMENT '更新人ID',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_role_code` (`role_code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色表';

-- 系统权限表
CREATE TABLE `sys_permission` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '权限ID，主键UUID',
    `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码，如：school_management、class_management',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_type` VARCHAR(20) DEFAULT 'MENU' COMMENT '权限类型：MENU菜单 BUTTON按钮 API接口',
    `parent_id` VARCHAR(32) COMMENT '父权限ID，构建权限树',
    `menu_path` VARCHAR(200) COMMENT '菜单路径，如：/class',
    `menu_icon` VARCHAR(50) COMMENT '菜单图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序字段',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_by` VARCHAR(32) COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) COMMENT '更新人ID',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_permission_code` (`permission_code`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统权限表';

-- 角色权限关联表
CREATE TABLE `sys_role_permission` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(32) NOT NULL COMMENT '权限ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- 用户角色关联表扩展（原有user_role表的增强版）
CREATE TABLE `sys_user_role` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID（可以是admin_user.id或u_student.id）',
    `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID',
    `user_type` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '用户类型：ADMIN管理员 STUDENT学生 TEACHER教师',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(32) COMMENT '创建人ID',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_user_type` (`user_type`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- 初始化系统角色数据
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `is_system`, `sort_order`, `status`) VALUES
('admin_role_001', 'admin', '系统管理员', '拥有系统所有权限，可以管理用户、角色、权限等', 1, 1, 1),
('teacher_role_002', 'teacher', '教师', '教师角色，可以管理课程、学生、作业等教学相关功能', 1, 2, 1),
('bbs_admin_role_003', 'bbs_admin', 'BBS管理员', '论坛管理员，可以管理论坛分类、敏感词等', 1, 3, 1);

-- 初始化管理员用户数据
INSERT INTO `admin_user` (`id`, `username`, `password`) VALUES
('admin_user_001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEIVa'), -- 密码: admin123
('admin_user_002', 'system', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEIVa'), -- 密码: admin123
('admin_user_003', 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEIVa'); -- 密码: admin123

-- 为管理员用户分配角色
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `user_type`) VALUES
('user_role_001', 'admin_user_001', 'admin_role_001', 'ADMIN'),  -- admin用户 -> 系统管理员角色
('user_role_002', 'admin_user_002', 'teacher_role_002', 'ADMIN'), -- system用户 -> 教师角色
('user_role_003', 'admin_user_003', 'bbs_admin_role_003', 'ADMIN'); -- manager用户 -> BBS管理员角色

-- 初始化系统权限数据
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `menu_icon`, `sort_order`, `status`) VALUES
-- 一级菜单
('perm_001', 'school_management', '学校教学管理', 'MENU', NULL, '/school', 'User', 1, 1),
('perm_002', 'system_settings', '系统设置', 'MENU', NULL, '/system', 'Notebook', 2, 1),
('perm_003', 'dorm_management', '宿舍信息', 'MENU', NULL, '/dorm', 'House', 3, 1),
('perm_004', 'course_management', '课程管理', 'MENU', NULL, '/course/management', 'Notebook', 4, 1),
('perm_005', 'ai_management', 'AI对话管理', 'MENU', NULL, '/ai', 'ChatDotRound', 5, 1),
('perm_006', 'word_management', '单词管理', 'MENU', NULL, '/word', 'Notebook', 6, 1),
('perm_007', 'video_management', '入学必看视频管理', 'MENU', NULL, '/video', 'VideoCamera', 7, 1),
('perm_008', 'group_management', '群组管理', 'MENU', NULL, '/group', 'User', 8, 1),
('perm_009', 'signin_management', '签到管理', 'MENU', NULL, '/signin', 'Notebook', 9, 1),
('perm_010', 'homework_management', '作业管理', 'MENU', NULL, '/homework', 'Notebook', 10, 1),
('perm_011', 'material_management', '资料管理', 'MENU', NULL, '/material', 'Files', 11, 1),
('perm_012', 'bbs_category_management', '校园论坛分类管理', 'MENU', NULL, '/bbs-category', 'Management', 12, 1),
('perm_013', 'bbs_sensitive_management', '校园论坛敏感词', 'MENU', NULL, '/bbs-sensitive', 'ChatDotRound', 13, 1),

-- 学校教学管理子菜单
('perm_101', 'class_management', '班级管理', 'MENU', 'perm_001', '/class', 'Management', 1, 1),
('perm_102', 'teacher_management', '教师管理', 'MENU', 'perm_001', '/teacher', 'Avatar', 2, 1),
('perm_103', 'student_management', '学生信息管理', 'MENU', 'perm_001', NULL, 'UserFilled', 3, 1),
('perm_104', 'student_unaudited', '未审批学生', 'MENU', 'perm_103', '/student/unaudited', 'User', 1, 1),
('perm_105', 'student_all', '全部学生', 'MENU', 'perm_103', '/student/all', 'User', 2, 1),

-- 系统设置子菜单
('perm_201', 'operation_log', '操作日志', 'MENU', 'perm_002', '/operlog', 'Files', 1, 1),
('perm_202', 'file_management', '文件管理', 'MENU', 'perm_002', '/file', 'Files', 2, 1),
('perm_203', 'online_users', '在线用户', 'MENU', 'perm_002', '/online', 'Monitor', 3, 1),
('perm_204', 'announcement_management', '公告管理', 'MENU', 'perm_002', '/announcement', 'Notebook', 4, 1),
('perm_205', 'school_info', '学校信息管理', 'MENU', 'perm_002', '/schoolinfo', 'Management', 5, 1),
('perm_206', 'bbs_admin_management', 'BBS管理员', 'MENU', 'perm_002', '/bbs', 'ChatDotRound', 6, 1),
('perm_207', 'role_permission_management', '角色权限管理', 'MENU', 'perm_002', '/role-permission', 'Setting', 7, 1),
('perm_208', 'admin_user_management', '管理员用户管理', 'MENU', 'perm_002', '/admin-user', 'User', 8, 1),
('perm_209', 'menu_management', '菜单权限管理', 'MENU', 'perm_002', '/menu-management', 'Setting', 9, 1),

-- 宿舍信息子菜单
('perm_301', 'dorm_building', '宿舍楼管理', 'MENU', 'perm_003', '/dorm/building', 'House', 1, 1),
('perm_302', 'dorm_room', '宿舍房间管理', 'MENU', 'perm_003', '/dorm/room', 'House', 2, 1),
('perm_303', 'dorm_bed', '床位管理', 'MENU', 'perm_003', '/dorm/bed', 'House', 3, 1);

-- 初始化角色权限关联数据
-- 管理员拥有所有权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) 
SELECT 
    CONCAT('rp_admin_', ROW_NUMBER() OVER (ORDER BY p.id)) as id,
    'admin_role_001' as role_id, 
    p.id as permission_id 
FROM `sys_permission` p;

-- 教师拥有教学相关权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES
('rp_teacher_001', 'teacher_role_002', 'perm_008'), -- 群组管理
('rp_teacher_002', 'teacher_role_002', 'perm_009'), -- 签到管理
('rp_teacher_003', 'teacher_role_002', 'perm_010'), -- 作业管理
('rp_teacher_004', 'teacher_role_002', 'perm_011'); -- 资料管理

-- BBS管理员拥有论坛管理权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES
('rp_bbs_001', 'bbs_admin_role_003', 'perm_012'), -- 校园论坛分类管理
('rp_bbs_002', 'bbs_admin_role_003', 'perm_013'); -- 校园论坛敏感词管理

-- 创建索引以提高查询性能
CREATE INDEX `idx_sys_permission_parent_id` ON `sys_permission` (`parent_id`);
CREATE INDEX `idx_sys_permission_type` ON `sys_permission` (`permission_type`);
CREATE INDEX `idx_sys_role_permission_role` ON `sys_role_permission` (`role_id`);
CREATE INDEX `idx_sys_role_permission_permission` ON `sys_role_permission` (`permission_id`);
CREATE INDEX `idx_sys_user_role_user` ON `sys_user_role` (`user_id`);
CREATE INDEX `idx_sys_user_role_role` ON `sys_user_role` (`role_id`);
