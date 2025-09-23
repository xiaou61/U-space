-- 1. 敏感词分类表
CREATE TABLE IF NOT EXISTS sensitive_category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词分类表';

-- 2. 敏感词表
CREATE TABLE IF NOT EXISTS sensitive_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(255) NOT NULL COMMENT '敏感词',
    category_id INT NOT NULL COMMENT '分类ID',
    level TINYINT DEFAULT 1 COMMENT '风险等级 1-低 2-中 3-高',
    action TINYINT DEFAULT 1 COMMENT '处理动作 1-替换 2-拒绝 3-审核',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    creator_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_status (category_id, status),
    INDEX idx_update_time (update_time),
    INDEX idx_word (word),
    FOREIGN KEY (category_id) REFERENCES sensitive_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- 3. 敏感词检测日志表
CREATE TABLE IF NOT EXISTS sensitive_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    module VARCHAR(50) NOT NULL COMMENT '模块名称',
    business_id BIGINT COMMENT '业务ID',
    original_text TEXT COMMENT '原始文本',
    hit_words JSON COMMENT '命中的敏感词',
    action VARCHAR(20) COMMENT '执行的动作',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_module_time (module, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词检测日志表';

-- ======================================
-- 初始化基础数据
-- ======================================

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