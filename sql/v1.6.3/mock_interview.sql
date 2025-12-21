-- ===================================
-- AI模拟面试模块数据库脚本
-- 版本: v1.0.0
-- 创建时间: 2025-12-21
-- ===================================

-- -----------------------------------
-- 1. 面试会话表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS mock_interview_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    direction VARCHAR(50) NOT NULL COMMENT '面试方向（java/frontend/python等）',
    level TINYINT NOT NULL COMMENT '难度级别：1-初级 2-中级 3-高级',
    interview_type TINYINT DEFAULT 1 COMMENT '面试类型：1-技术 2-综合 3-专项',
    style TINYINT DEFAULT 2 COMMENT 'AI风格：1-温和 2-标准 3-压力',
    question_count INT NOT NULL COMMENT '题目数量',
    question_mode TINYINT DEFAULT 2 COMMENT '出题模式：1-本地题库 2-AI出题',
    duration_minutes INT COMMENT '预计时长（分钟）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-进行中 1-已完成 2-已中断',
    total_score INT COMMENT '总分（满分100）',
    knowledge_score INT COMMENT '知识得分',
    depth_score INT COMMENT '深度得分',
    expression_score INT COMMENT '表达得分',
    adaptability_score INT COMMENT '应变得分',
    ai_summary TEXT COMMENT 'AI总体评价',
    ai_suggestion TEXT COMMENT 'AI改进建议',
    current_question_order INT DEFAULT 0 COMMENT '当前题目序号',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_user_status (user_id, status),
    INDEX idx_direction (direction),
    INDEX idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟面试会话表';

-- -----------------------------------
-- 2. 面试问答记录表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS mock_interview_qa (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT COMMENT '关联的题库题目ID（可为空）',
    question_order INT NOT NULL COMMENT '题目序号',
    question_content TEXT NOT NULL COMMENT '问题内容',
    question_type TINYINT DEFAULT 1 COMMENT '问题类型：1-主问题 2-追问',
    parent_qa_id BIGINT COMMENT '父问答ID（追问时关联主问题）',
    user_answer TEXT COMMENT '用户回答',
    answer_duration_seconds INT COMMENT '回答用时（秒）',
    score INT COMMENT '得分（0-10）',
    ai_feedback TEXT COMMENT 'AI反馈（JSON格式）',
    reference_answer TEXT COMMENT '参考答案',
    knowledge_points VARCHAR(500) COMMENT '考察知识点（逗号分隔）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待回答 1-已回答 2-已跳过',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_parent_qa_id (parent_qa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟面试问答记录表';

-- -----------------------------------
-- 3. 面试方向配置表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS mock_interview_direction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    direction_code VARCHAR(50) NOT NULL COMMENT '方向代码（java/frontend等）',
    direction_name VARCHAR(100) NOT NULL COMMENT '方向名称',
    icon VARCHAR(50) COMMENT '图标',
    description VARCHAR(500) COMMENT '描述',
    category_ids VARCHAR(200) COMMENT '关联的题库分类ID（逗号分隔）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_direction_code (direction_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟面试方向配置表';

-- -----------------------------------
-- 4. 用户面试统计表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS mock_interview_user_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_interviews INT DEFAULT 0 COMMENT '总面试次数',
    completed_interviews INT DEFAULT 0 COMMENT '完成面试次数',
    avg_score DECIMAL(5,2) DEFAULT 0 COMMENT '平均分',
    highest_score INT DEFAULT 0 COMMENT '最高分',
    total_questions INT DEFAULT 0 COMMENT '总回答题数',
    correct_questions INT DEFAULT 0 COMMENT '高分题数（>=7分）',
    interview_streak INT DEFAULT 0 COMMENT '连续面试天数',
    max_streak INT DEFAULT 0 COMMENT '最长连续天数',
    last_interview_date DATE COMMENT '最后面试日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户面试统计表';

-- -----------------------------------
-- 5. 初始化面试方向配置数据
-- -----------------------------------
INSERT INTO mock_interview_direction (direction_code, direction_name, icon, description, sort_order, status) VALUES
('java', 'Java 后端', '☕', 'Java后端开发面试，涵盖Java基础、JVM、并发、Spring等', 1, 1),
('frontend', '前端开发', '🌐', '前端开发面试，涵盖HTML/CSS/JS、Vue/React、浏览器原理等', 2, 1),
('python', 'Python 开发', '🐍', 'Python开发面试，涵盖Python基础、Django/Flask、爬虫等', 3, 1),
('go', 'Go 开发', '🔷', 'Go语言开发面试，涵盖Go基础、并发编程、微服务等', 4, 1),
('fullstack', '全栈开发', '🔄', '全栈开发面试，涵盖前后端技术栈、系统设计等', 5, 1),
('database', '数据库', '🗄️', '数据库面试，涵盖MySQL、Redis、MongoDB等', 6, 1),
('devops', 'DevOps', '🔧', 'DevOps面试，涵盖Linux、Docker、K8s、CI/CD等', 7, 1),
('algorithm', '算法', '🧮', '算法面试，涵盖数据结构、算法思想、LeetCode等', 8, 1);
