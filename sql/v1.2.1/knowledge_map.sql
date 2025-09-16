-- 1. 知识图谱表
CREATE TABLE knowledge_map (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图谱ID',
    title VARCHAR(200) NOT NULL COMMENT '图谱标题',
    description TEXT COMMENT '图谱描述',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    user_id BIGINT NOT NULL COMMENT '创建用户ID(管理员)',
    node_count INT DEFAULT 0 COMMENT '节点总数',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    status TINYINT(1) DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布 2-已隐藏',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识图谱表';

-- 2. 知识节点表  
CREATE TABLE knowledge_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点ID',
    map_id BIGINT NOT NULL COMMENT '所属图谱ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父节点ID，0为根节点',
    title VARCHAR(200) NOT NULL COMMENT '节点标题',
    content LONGTEXT COMMENT '节点详细内容(Markdown格式)',
    node_type TINYINT(1) DEFAULT 1 COMMENT '节点类型: 1-普通 2-重点 3-难点',
    sort_order INT DEFAULT 0 COMMENT '同级排序序号',
    level_depth INT DEFAULT 1 COMMENT '层级深度',
    is_expanded TINYINT(1) DEFAULT 1 COMMENT '是否默认展开',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    last_view_time DATETIME COMMENT '最后查看时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_map_id (map_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_level_depth (level_depth),
    FOREIGN KEY (map_id) REFERENCES knowledge_map(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识节点表';
