-- 头像功能增强 SQL 脚本
-- 用于增强用户头像功能的数据库更新

-- 1. 检查用户表是否已有avatar字段（如果没有则添加）
-- 注意：根据现有代码，avatar字段应该已经存在

-- 2. 为现有用户设置默认头像（如果avatar为空）
-- 使用统一的默认头像URL
UPDATE user_info 
SET avatar = 'https://cn.cravatar.com/avatar/e1e7ba949ade0936e071132d2edd3b3c.png'
WHERE avatar IS NULL OR avatar = '';

-- 3. 查询统计信息
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN avatar IS NOT NULL AND avatar != '' THEN 1 END) as users_with_avatar,
    COUNT(CASE WHEN avatar IS NULL OR avatar = '' THEN 1 END) as users_without_avatar
FROM user_info 
WHERE status != 2;

-- 4. 查看头像使用情况
SELECT 
    CASE 
        WHEN avatar = 'https://cn.cravatar.com/avatar/e1e7ba949ade0936e071132d2edd3b3c.png' THEN '默认头像'
        WHEN avatar IS NULL OR avatar = '' THEN '无头像'
        ELSE '自定义头像'
    END as avatar_type,
    COUNT(*) as count
FROM user_info 
WHERE status != 2
GROUP BY avatar_type; 