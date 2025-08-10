CREATE TABLE u_vote_activity
(
    id          VARCHAR(32) PRIMARY KEY COMMENT '活动ID',
    title       VARCHAR(255) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    start_time  DATETIME     NOT NULL COMMENT '开始时间',
    end_time    DATETIME     NOT NULL COMMENT '结束时间',
    status      TINYINT(1)            DEFAULT 1 COMMENT '状态（1=进行中，0=已结束，-1=草稿）',
    create_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='投票活动表';
CREATE TABLE u_vote_option
(
    id          VARCHAR(32) PRIMARY KEY COMMENT '选项ID',
    activity_id VARCHAR(32)  NOT NULL COMMENT '活动ID',
    option_name VARCHAR(255) NOT NULL COMMENT '选项名称',
    option_desc TEXT COMMENT '选项描述',
    image_url   VARCHAR(500)          DEFAULT NULL COMMENT '选项图片',
    vote_count  INT                   DEFAULT 0 COMMENT '票数',
    create_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='投票选项表';
CREATE TABLE u_vote_record
(
    id          VARCHAR(32) PRIMARY KEY COMMENT '记录ID',
    activity_id VARCHAR(32) NOT NULL COMMENT '活动ID',
    option_id   VARCHAR(32) NOT NULL COMMENT '选项ID',
    user_id     VARCHAR(32) NOT NULL COMMENT '用户ID',
    vote_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    CONSTRAINT uq_activity_user UNIQUE (activity_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='投票记录表';

-- 添加活动管理一级菜单权限
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `menu_icon`, `sort_order`, `status`) VALUES
    ('perm_014', 'activity_management', '活动相关', 'MENU', NULL, '/activity', 'Trophy', 14, 1);

-- 添加投票活动二级菜单权限
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `menu_icon`, `sort_order`, `status`) VALUES
    ('perm_401', 'vote_activity_management', '投票活动', 'MENU', 'perm_014', '/activity/vote', 'Trophy', 1, 1);

-- 添加投票活动管理的具体操作权限
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `menu_icon`, `sort_order`, `status`) VALUES
                                                                                                                                                                ('perm_4001', 'vote_activity_list', '查看投票活动列表', 'BUTTON', 'perm_401', NULL, NULL, 1, 1),
                                                                                                                                                                ('perm_4002', 'vote_activity_add', '添加投票活动', 'BUTTON', 'perm_401', NULL, NULL, 2, 1),
                                                                                                                                                                ('perm_4003', 'vote_activity_edit', '编辑投票活动', 'BUTTON', 'perm_401', NULL, NULL, 3, 1),
                                                                                                                                                                ('perm_4004', 'vote_activity_offline', '下架投票活动', 'BUTTON', 'perm_401', NULL, NULL, 4, 1),
                                                                                                                                                                ('perm_4005', 'vote_option_add', '添加投票选项', 'BUTTON', 'perm_401', NULL, NULL, 5, 1),
                                                                                                                                                                ('perm_4006', 'vote_option_edit', '编辑投票选项', 'BUTTON', 'perm_401', NULL, NULL, 6, 1),
                                                                                                                                                                ('perm_4007', 'vote_option_delete', '删除投票选项', 'BUTTON', 'perm_401', NULL, NULL, 7, 1);
