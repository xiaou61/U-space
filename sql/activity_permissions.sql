INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `menu_icon`, `sort_order`, `status`) VALUES

-- ==================== 一级菜单 ====================
('perm_activity_001', 'activity_system', '活动管理系统', 'MENU', NULL, '/activity', 'Star', 20, 1),

-- ==================== 二级菜单 ====================
-- 活动管理模块
('perm_activity_101', 'activity_management', '活动管理', 'MENU', 'perm_activity_001', '/activity/management', 'Calendar', 1, 1),
-- 积分管理模块
('perm_activity_102', 'points_management', '积分管理', 'MENU', 'perm_activity_001', '/activity/points', 'Coin', 2, 1)
