

-- Bug条目表
DROP TABLE IF EXISTS `bug_item`;
CREATE TABLE `bug_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Bug ID',
  `title` varchar(200) NOT NULL COMMENT 'Bug标题',
  `phenomenon` text NOT NULL COMMENT 'Bug现象描述',
  `cause_analysis` text NOT NULL COMMENT '原因分析',
  `solution` text NOT NULL COMMENT '解决方案',
  `tech_tags` varchar(500) DEFAULT NULL COMMENT '技术标签（多个标签用逗号分隔）',
  `difficulty_level` tinyint NOT NULL DEFAULT '1' COMMENT '难度等级：1-初级，2-中级，3-高级，4-专家级',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序值（数字越大越靠前）',
  `created_by` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_difficulty_level` (`difficulty_level`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_tech_tags` (`tech_tags`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bug条目表';

-- 用户Bug浏览历史表
DROP TABLE IF EXISTS `user_bug_history`;
CREATE TABLE `user_bug_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `bug_id` bigint NOT NULL COMMENT 'Bug ID',
  `view_time` datetime NOT NULL COMMENT '浏览时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_bug` (`user_id`, `bug_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_bug_id` (`bug_id`),
  KEY `idx_view_time` (`view_time`),
  KEY `idx_user_view_time` (`user_id`, `view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户Bug浏览历史表';

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
