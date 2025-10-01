/*
 Navicat Premium Data Transfer

 Source Server         : 本机的
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : code_nest

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 03/09/2025 13:33:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Records of sys_admin_role
-- ----------------------------
INSERT INTO `sys_admin_role` VALUES (1, 1, 1, '2025-08-31 12:11:46', 1);
INSERT INTO `sys_admin_role` VALUES (2, 2, 2, '2025-08-31 12:11:46', 1);


-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 'admin', '$2a$10$w9fmcYcV69fM7.01Ev6N8OwlUeShcb8Zc1B0uDNc6Q4s0z8CzvQVi', '超级管理员', 'admin@code-nest.com', '13800138000', NULL, 1, 0, '2025-09-03 11:17:28', '127.0.0.1', 11, '系统内置超级管理员账户', '2025-08-31 12:11:46', '2025-09-03 11:17:27', 1, NULL);
INSERT INTO `sys_admin` VALUES (2, 'system', '$2a$10$w9fmcYcV69fM7.01Ev6N8OwlUeShcb8Zc1B0uDNc6Q4s0z8CzvQVi', '系统管理员', 'system@code-nest.com', '13800138001', NULL, 1, 0, NULL, NULL, 0, '系统内置系统管理员账户', '2025-08-31 12:11:46', '2025-08-31 12:28:30', 1, NULL);


-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 0, 1, '2025-08-31 12:11:46', '2025-08-31 12:11:46', NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '系统管理员', 'SYSTEM_ADMIN', '系统管理员，负责系统配置和用户管理', 0, 2, '2025-08-31 12:11:46', '2025-08-31 12:11:46', NULL, NULL);
INSERT INTO `sys_role` VALUES (3, '普通管理员', 'ADMIN', '普通管理员，负责日常业务管理', 0, 3, '2025-08-31 12:11:46', '2025-08-31 12:11:46', NULL, NULL);


-- 插入默认本地存储配置
INSERT INTO `storage_config` (`storage_type`, `config_name`, `config_params`, `is_default`, `is_enabled`, `test_status`) VALUES
    ('LOCAL', '本地存储', '{"basePath":"/uploads","urlPrefix":"http://localhost:9999/files"}', 1, 1, 1);

-- 插入默认系统设置
INSERT INTO `file_system_setting` (`setting_key`, `setting_value`, `setting_desc`) VALUES
                                                                                       ('MAX_FILE_SIZE', '104857600', '最大文件大小限制(字节) - 100MB'),
                                                                                       ('ALLOWED_FILE_TYPES', '["jpg","jpeg","png","gif","bmp","webp","pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip","rar","7z","mp4","avi","mov","mp3","wav"]', '允许上传的文件类型白名单'),
                                                                                       ('AUTO_BACKUP_ENABLED', 'true', '是否自动创建本地备份'),
                                                                                       ('TEMP_LINK_EXPIRE_HOURS', '24', '临时链接有效期(小时)'),
                                                                                       ('STORAGE_QUOTA_PER_MODULE', '10737418240', '每个模块存储配额(字节) - 10GB');



-- 初始化消息模板数据
INSERT INTO notification_template (code, name, title_template, content_template) VALUES
                                                                                     ('WELCOME', '欢迎消息', '欢迎加入{platform}', '亲爱的{username}，欢迎加入我们的平台！'),
                                                                                     ('COMMUNITY_LIKE', '帖子点赞', '您的帖子收到点赞', '您的帖子《{postTitle}》收到了{likerName}的点赞'),
                                                                                     ('COMMUNITY_COMMENT', '帖子评论', '您的帖子收到评论', '您的帖子《{postTitle}》收到了{commenterName}的评论'),
                                                                                     ('INTERVIEW_FAVORITE', '面试题收藏', '收藏提醒', '您收藏的面试题《{questionTitle}》已更新'),
                                                                                     ('SYSTEM_MAINTENANCE', '系统维护', '系统维护通知', '系统将于{maintenanceTime}进行维护，预计耗时{duration}');


SET FOREIGN_KEY_CHECKS = 1;

-- 插入默认分类
INSERT IGNORE INTO sensitive_category (id, name, description, sort_order, status) VALUES
                                                                                      (1, '政治敏感', '涉及政治敏感内容的词汇', 1, 1),
                                                                                      (2, '色情低俗', '涉及色情、低俗内容的词汇', 2, 1),
                                                                                      (3, '人身攻击', '涉及人身攻击、辱骂的词汇', 3, 1),
                                                                                      (4, '广告推广', '涉及广告推广、营销的词汇', 4, 1),
                                                                                      (5, '其他违规', '其他违规内容的词汇', 5, 1);

-- 插入一些示例敏感词（用于测试）
INSERT IGNORE INTO sensitive_word (word, category_id, level, action, status, creator_id) VALUES
                                                                                             ('测试敏感词1', 3, 1, 1, 1, 1),
                                                                                             ('测试敏感词2', 3, 2, 2, 1, 1),
                                                                                             ('测试敏感词3', 3, 3, 3, 1, 1),
                                                                                             ('垃圾', 3, 1, 1, 1, 1),
                                                                                             ('广告', 4, 1, 1, 1, 1);

-- 初始化Bug数据
INSERT INTO `bug_item` (`title`, `phenomenon`, `cause_analysis`, `solution`, `tech_tags`, `difficulty_level`, `status`, `sort_order`, `created_by`) VALUES
                                                                                                                                                        ('空指针异常 (NullPointerException)',
                                                                                                                                                         '程序运行时抛出 java.lang.NullPointerException，通常发生在调用 null 对象的方法或访问其属性时。',
                                                                                                                                                         '1. 对象未正确初始化\n2. 方法返回了 null 值但未做检查\n3. 集合或数组元素为 null\n4. 链式调用中某个环节返回 null',
                                                                                                                                                         '1. 使用 null 检查：if (obj != null)\n2. 使用 Optional 类处理可能为 null 的值\n3. 初始化时给对象赋默认值\n4. 使用断言或参数验证\n5. IDE工具静态检查',
                                                                                                                                                         'Java,异常处理,空指针', 1, 1, 100, 1),

                                                                                                                                                        ('内存泄漏 (Memory Leak)',
                                                                                                                                                         '程序运行时间越长，占用内存越来越多，最终可能导致内存溢出 OutOfMemoryError。',
                                                                                                                                                         '1. 集合类持有对象引用未清理\n2. 监听器未注销\n3. 静态变量持有大对象\n4. 线程未正确关闭\n5. 缓存无限制增长',
                                                                                                                                                         '1. 及时清理不再使用的对象引用\n2. 使用弱引用 WeakReference\n3. 监听器使用完后注销\n4. 限制缓存大小并设置过期策略\n5. 使用内存分析工具如 JProfiler',
                                                                                                                                                         'Java,内存管理,性能优化', 3, 1, 95, 1),

                                                                                                                                                        ('并发修改异常 (ConcurrentModificationException)',
                                                                                                                                                         '在遍历集合时对集合进行修改操作，抛出 ConcurrentModificationException。',
                                                                                                                                                         '多线程环境下或单线程中遍历时修改集合结构，导致迭代器的预期修改计数与实际不符。',
                                                                                                                                                         '1. 使用迭代器的 remove() 方法删除元素\n2. 使用 ConcurrentHashMap 等线程安全集合\n3. 先收集要修改的元素，遍历结束后再修改\n4. 使用 Collections.synchronizedList() 包装',
                                                                                                                                                         'Java,多线程,集合框架', 2, 1, 90, 1),

                                                                                                                                                        ('死锁 (Deadlock)',
                                                                                                                                                         '程序运行过程中多个线程相互等待对方释放资源，导致程序卡死无法继续执行。',
                                                                                                                                                         '两个或多个线程按不同顺序获取多个锁资源，形成循环等待的情况。',
                                                                                                                                                         '1. 统一锁的获取顺序\n2. 使用超时锁 tryLock(timeout)\n3. 减少锁的持有时间\n4. 避免嵌套锁\n5. 使用死锁检测工具',
                                                                                                                                                         'Java,多线程,锁机制', 4, 1, 85, 1),

                                                                                                                                                        ('栈溢出 (StackOverflowError)',
                                                                                                                                                         '程序运行时抛出 java.lang.StackOverflowError，通常在递归调用时发生。',
                                                                                                                                                         '1. 递归没有正确的终止条件\n2. 递归层次过深\n3. 方法调用链太长\n4. 局部变量占用空间过大',
                                                                                                                                                         '1. 检查递归终止条件\n2. 优化递归算法，考虑使用迭代\n3. 增加JVM栈内存大小 -Xss\n4. 将递归改为尾递归优化',
                                                                                                                                                         'Java,递归,内存管理', 2, 1, 80, 1);


-- 插入一些初始数据

-- 程序员节日事件数据
INSERT INTO `developer_calendar_event` (`event_name`, `event_date`, `event_type`, `description`, `icon`, `color`, `is_major`, `blessing_text`) VALUES
                                                                                                                                                   ('程序员节', '10-24', 1, '1024程序员节，因为1024是2的十次方，是一个特殊的数字，在信息技术中有特殊含义', 'code', '#722ED1', 1, '1024程序员节快乐！愿代码无Bug，愿人生多惊喜！'),
                                                                                                                                                   ('世界编程日', '09-13', 1, '庆祝编程技术发展，激励更多人投身编程事业', 'terminal', '#13C2C2', 1, '世界编程日快乐！用代码改变世界，用技术创造未来！'),
                                                                                                                                                   ('Java诞生日', '05-23', 2, '1995年5月23日，Java编程语言正式发布', 'coffee', '#F5A623', 1, '感谢Java为编程世界带来的贡献！Write Once, Run Anywhere!'),
                                                                                                                                                   ('Python诞生日', '02-20', 2, '1991年2月20日，Python首个版本发布', 'python', '#4CAF50', 1, 'Python让编程变得简单而优雅！人生苦短，我用Python！'),
                                                                                                                                                   ('GitHub上线日', '04-10', 3, '2008年4月10日，GitHub正式上线，改变了开源协作方式', 'github', '#24292E', 1, '感谢GitHub让全世界开发者能够协作共建！'),
                                                                                                                                                   ('Linux诞生日', '08-25', 3, '1991年8月25日，Linus首次发布Linux内核', 'linux', '#FD7E14', 1, '致敬Linux！开源改变世界，自由万岁！'),
                                                                                                                                                   ('JavaScript诞生日', '05-05', 2, '1995年5月，JavaScript在Netscape浏览器中首次亮相', 'javascript', '#F7DF1E', 0, 'JavaScript让Web变得生动有趣！'),
                                                                                                                                                   ('C语言诞生日', '01-01', 2, '1972年，C语言在贝尔实验室诞生，影响深远的编程语言', 'c', '#00599C', 1, 'C语言是现代编程的基石！'),
                                                                                                                                                   ('Git诞生日', '04-07', 3, '2005年4月7日，Linus创建了Git版本控制系统', 'git', '#F05032', 0, 'Git让代码版本管理变得简单高效！'),
                                                                                                                                                   ('Stack Overflow上线日', '09-15', 3, '2008年9月15日，程序员问答社区Stack Overflow上线', 'stackoverflow', '#F47F24', 0, '感谢Stack Overflow帮助无数程序员解决问题！');

-- 每日内容数据
INSERT INTO `daily_content` (`content_type`, `title`, `content`, `author`, `programming_language`, `tags`, `difficulty_level`) VALUES
                                                                                                                                   (1, '简洁胜过复杂', '简单胜过复杂，复杂胜过混乱。', 'Tim Peters', 'Python', '["设计原则", "Python之禅"]', 1),
                                                                                                                                   (1, '代码如诗', '代码应该写得像诗一样优美，让人读起来赏心悦目。', '佚名', NULL, '["代码质量", "编程哲学"]', 1),
                                                                                                                                   (2, '使用有意义的变量名', '变量名应该清楚地表达其用途，避免使用a、b、c这样的无意义名称。', NULL, NULL, '["代码规范", "可读性"]', 1),
                                                                                                                                   (2, '遵循单一职责原则', '一个函数或类应该只有一个引起变化的原因，专注于做好一件事。', NULL, NULL, '["设计模式", "SOLID原则"]', 2),
                                                                                                                                   (3, 'Python列表推导式', '# 使用列表推导式生成平方数\nsquares = [x**2 for x in range(10)]\nprint(squares)  # [0, 1, 4, 9, 16, 25, 36, 49, 64, 81]', NULL, 'Python', '["Python", "列表推导式"]', 1),
                                                                                                                                   (3, 'JavaScript箭头函数', '// ES6箭头函数简化代码\nconst add = (a, b) => a + b;\nconst multiply = (a, b) => a * b;\nconsole.log(add(2, 3)); // 5', NULL, 'JavaScript', '["JavaScript", "ES6", "箭头函数"]', 1),
                                                                                                                                   (4, '第一台电子计算机诞生', '1946年2月14日，世界上第一台通用电子计算机ENIAC在美国宾夕法尼亚大学诞生。', NULL, NULL, '["计算机历史", "ENIAC"]', 1),
                                                                                                                                   (4, '万维网诞生', '1989年3月12日，蒂姆·伯纳斯-李提出了万维网(World Wide Web)的构想。', 'Tim Berners-Lee', NULL, '["互联网历史", "WWW"]', 1);
