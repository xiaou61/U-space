-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `u-space` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用该数据库
USE `u-space`;

-- 创建管理员用户表
CREATE TABLE `admin_user`
(
    `id`       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员用户表';

CREATE TABLE user_role
(
    id   BIGINT PRIMARY KEY COMMENT '主键ID',
    role VARCHAR(50) NOT NULL COMMENT '角色名称'
) COMMENT ='用户角色表';



DROP TABLE IF EXISTS u_college;

CREATE TABLE u_college
(
    college_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学院编号（主键）',
    name         VARCHAR(255) NOT NULL COMMENT '学院名称',
    dean         VARCHAR(100) DEFAULT NULL COMMENT '院长姓名',
    phone        VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    email        VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    campus_name  VARCHAR(255) NOT NULL COMMENT '所属校区名称',
    created_by   VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   VARCHAR(32)  DEFAULT NULL COMMENT '更新人',
    updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (college_id),
    KEY idx_name (name),
    KEY idx_campus_name (campus_name)
) COMMENT ='学院信息表';


CREATE TABLE u_major
(
    major_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '专业编号（主键）',
    name            VARCHAR(255) NOT NULL COMMENT '专业名称',
    code            VARCHAR(50)  NOT NULL COMMENT '专业代码',
    college_id      BIGINT       NOT NULL COMMENT '所属学院ID',
    leader          VARCHAR(100)                    DEFAULT NULL COMMENT '专业负责人',
    education_level ENUM ('本科', '专科', '研究生') DEFAULT '本科' COMMENT '学历层次',
    status          ENUM ('在办', '停办', '筹建')   DEFAULT '在办' COMMENT '专业状态',
    created_by      VARCHAR(32)                     DEFAULT NULL COMMENT '创建人',
    created_time    DATETIME                        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(32)                     DEFAULT NULL COMMENT '更新人',
    updated_time    DATETIME                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (major_id),
    KEY idx_name (name),
    KEY idx_college (college_id),
    CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES u_college (college_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT ='专业信息表';


DROP TABLE IF EXISTS u_class;

CREATE TABLE u_class
(
    class_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '班级编号（主键）',
    name         VARCHAR(100) NOT NULL COMMENT '班级名称',
    major_id     BIGINT       NOT NULL COMMENT '所属专业ID',
    grade_year   YEAR         NOT NULL COMMENT '入学年份',
    class_no     VARCHAR(20)                   DEFAULT NULL COMMENT '班号（如01、A1）',
    head_teacher VARCHAR(100)                  DEFAULT NULL COMMENT '班主任',
    status       ENUM ('在读', '毕业', '停办') DEFAULT '在读' COMMENT '班级状态',
    created_by   VARCHAR(32)                   DEFAULT NULL COMMENT '创建人',
    created_time DATETIME                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   VARCHAR(32)                   DEFAULT NULL COMMENT '更新人',
    updated_time DATETIME                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (class_id),
    KEY idx_major (major_id),
    KEY idx_grade (grade_year),
    CONSTRAINT fk_class_major FOREIGN KEY (major_id) REFERENCES u_major (major_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT ='班级信息表';



DROP TABLE IF EXISTS u_student;

CREATE TABLE u_student
(
    student_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学生编号（主键）',
    student_number  VARCHAR(30)  NOT NULL UNIQUE COMMENT '学号',
    name            VARCHAR(100) NOT NULL COMMENT '姓名',
    gender          ENUM ('男', '女', '其他')             DEFAULT '男' COMMENT '性别',
    phone           VARCHAR(20)                           DEFAULT NULL COMMENT '联系电话',
    enrollment_year YEAR         NOT NULL COMMENT '入学年份',
    status          ENUM ('在读', '休学', '退学', '毕业') DEFAULT '在读' COMMENT '学籍状态',
    created_by      VARCHAR(32)                           DEFAULT NULL COMMENT '创建人',
    created_time    DATETIME                              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(32)                           DEFAULT NULL COMMENT '更新人',
    updated_time    DATETIME                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (student_id),
    UNIQUE KEY uq_student_number (student_number),
    KEY idx_name (name)
) COMMENT ='学生信息表';


DROP TABLE IF EXISTS u_student_info_link;

CREATE TABLE u_student_info_link
(
    link_id        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_number VARCHAR(30) NOT NULL COMMENT '学号',
    major_id       BIGINT      NOT NULL COMMENT '专业ID',
    class_id       BIGINT      NOT NULL COMMENT '班级ID',
    college_id     BIGINT      NOT NULL COMMENT '学院ID',
    created_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (link_id),
    UNIQUE KEY uq_student_number (student_number),
    KEY idx_major (major_id),
    KEY idx_class (class_id),
    KEY idx_college (college_id)
) COMMENT ='学生信息关联表';

-- u_student 主键 idx 已有
ALTER TABLE u_student_info_link
    ADD INDEX idx_student_number (student_number),
    ADD INDEX idx_college_id (college_id),
    ADD INDEX idx_major_id (major_id),
    ADD INDEX idx_class_id (class_id);



create table sys_oper_log
(
    oper_id        bigint(20) not null comment '日志主键',
    title          varchar(50)   default '' comment '模块标题',
    business_type  int(2)        default 0 comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(100)  default '' comment '方法名称',
    request_method varchar(10)   default '' comment '请求方式',
    operator_type  int(1)        default 0 comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' comment '操作人员',
    oper_url       varchar(255)  default '' comment '请求URL',
    oper_ip        varchar(128)  default '' comment '主机地址',
    oper_location  varchar(255)  default '' comment '操作地点',
    oper_param     varchar(4000) default '' comment '请求参数',
    json_result    varchar(4000) default '' comment '返回参数',
    status         int(1)        default 0 comment '操作状态（0正常 1异常）',
    error_msg      varchar(4000) default '' comment '错误消息',
    oper_time      datetime comment '操作时间',
    cost_time      bigint(20)    default 0 comment '消耗时间',
    primary key (oper_id),
    key idx_sys_oper_log_bt (business_type),
    key idx_sys_oper_log_s (status),
    key idx_sys_oper_log_ot (oper_time)
) engine = innodb comment = '操作日志记录';


#校园指南表
CREATE TABLE u_campus_guide
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title       VARCHAR(255) NOT NULL COMMENT '标题',
    content     TEXT COMMENT '内容描述',
    image_list  TEXT COMMENT '图片URL列表(JSON字符串)',
    keywords    TEXT COMMENT '关键词列表(JSON字符串)',
    category    VARCHAR(50) DEFAULT '校园指南' COMMENT '分类标签，例如 校历、迎新、食堂等',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)  DEFAULT 0 COMMENT '逻辑删除标记 0-正常 1-删除'
);


#文件详细信息表
CREATE TABLE `u_file_detail`
(
    `id`                varchar(32)  NOT NULL COMMENT '文件id',
    `url`               varchar(512) NOT NULL COMMENT '文件访问地址',
    `size`              bigint(20)   DEFAULT NULL COMMENT '文件大小，单位字节',
    `filename`          varchar(256) DEFAULT NULL COMMENT '文件名称',
    `original_filename` varchar(256) DEFAULT NULL COMMENT '原始文件名',
    `base_path`         varchar(256) DEFAULT NULL COMMENT '基础存储路径',
    `path`              varchar(256) DEFAULT NULL COMMENT '存储路径',
    `ext`               varchar(32)  DEFAULT NULL COMMENT '文件扩展名',
    `content_type`      varchar(128) DEFAULT NULL COMMENT 'MIME类型',
    `platform`          varchar(32)  DEFAULT NULL COMMENT '存储平台',
    `th_url`            varchar(512) DEFAULT NULL COMMENT '缩略图访问路径',
    `th_filename`       varchar(256) DEFAULT NULL COMMENT '缩略图名称',
    `th_size`           bigint(20)   DEFAULT NULL COMMENT '缩略图大小，单位字节',
    `th_content_type`   varchar(128) DEFAULT NULL COMMENT '缩略图MIME类型',
    `object_id`         varchar(32)  DEFAULT NULL COMMENT '文件所属对象id',
    `object_type`       varchar(32)  DEFAULT NULL COMMENT '文件所属对象类型，例如用户头像，评价图片',
    `metadata`          text COMMENT '文件元数据',
    `user_metadata`     text COMMENT '文件用户元数据',
    `th_metadata`       text COMMENT '缩略图元数据',
    `th_user_metadata`  text COMMENT '缩略图用户元数据',
    `attr`              text COMMENT '附加属性',
    `file_acl`          varchar(32)  DEFAULT NULL COMMENT '文件ACL',
    `th_file_acl`       varchar(32)  DEFAULT NULL COMMENT '缩略图文件ACL',
    `hash_info`         text COMMENT '哈希信息',
    `upload_id`         varchar(128) DEFAULT NULL COMMENT '上传ID，仅在手动分片上传时使用',
    `upload_status`     int(11)      DEFAULT NULL COMMENT '上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成',
    `create_time`       datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC COMMENT ='文件记录表';



CREATE TABLE u_place_category
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL COMMENT '分类名称',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by   VARCHAR(50)           DEFAULT NULL COMMENT '创建人',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by   VARCHAR(50)           DEFAULT NULL COMMENT '更新人'
) COMMENT ='地点分类表';

-- 地点信息表
CREATE TABLE u_building_info
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    name        VARCHAR(255) NOT NULL COMMENT '建筑名称',
    aliases     VARCHAR(255) DEFAULT NULL COMMENT '别名或用途说明',
    img         TEXT COMMENT '全景图 URL',
    images      JSON COMMENT '详情图列表，JSON 数组字符串',
    description TEXT COMMENT '建筑描述信息',
    latitude    DECIMAL(10, 7) COMMENT '纬度',
    longitude   DECIMAL(10, 7) COMMENT '经度',
    category_id BIGINT       NOT NULL COMMENT '分类ID',
    create_by   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_building_category FOREIGN KEY (category_id) REFERENCES u_place_category (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='建筑信息表';

#qa问答
CREATE TABLE `u_qa_pairs`
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    q  TEXT NOT NULL,
    a  TEXT NOT NULL
);

#app用户表
CREATE TABLE u_student_user
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_number VARCHAR(50)  NOT NULL COMMENT '学号',
    name           VARCHAR(100) NOT NULL COMMENT '姓名',
    password       VARCHAR(255) NOT NULL COMMENT '密码',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生用户表';

ALTER TABLE u_student_user
    ADD COLUMN avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像地址';
ALTER TABLE u_student_user
    ADD COLUMN email VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址';


#bbs帖子表
CREATE TABLE `u_post`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `user_id`       BIGINT       NOT NULL COMMENT '作者用户ID',
    `title`         VARCHAR(255) NOT NULL COMMENT '帖子标题',
    `content`       TEXT         NOT NULL COMMENT '帖子内容',
    `like_count`    INT        DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT        DEFAULT 0 COMMENT '评论数',
    `view_count`    INT        DEFAULT 0 COMMENT '浏览数',
    `status`        TINYINT    DEFAULT 1 COMMENT '状态（1:正常，0:禁用）',
    `is_deleted`    TINYINT(1) DEFAULT 0 COMMENT '是否已删除（0：未删除，1：已删除）',
    `create_time`   DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子表';
ALTER TABLE `u_post`
    ADD COLUMN `image_urls` JSON DEFAULT NULL COMMENT '图片地址列表（JSON数组）';
-- 添加分类字段
ALTER TABLE `u_post`
    ADD COLUMN `category` VARCHAR(64) DEFAULT NULL COMMENT '帖子分类';

-- 为分类字段添加索引
CREATE INDEX `idx_category` ON `u_post` (`category`);



CREATE TABLE u_post_like
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    post_id     BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
);



CREATE TABLE u_post_comment
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id     BIGINT NOT NULL COMMENT '帖子ID',
    parent_id   BIGINT   DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
    user_id     BIGINT NOT NULL COMMENT '评论者用户ID',
    content     TEXT   NOT NULL COMMENT '评论内容',
    like_count  INT      DEFAULT 0 COMMENT '点赞数',
    is_deleted  TINYINT  DEFAULT 0 COMMENT '是否删除 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);


CREATE TABLE u_post_comment_like
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    comment_id  BIGINT NOT NULL COMMENT '评论ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_user_comment (user_id, comment_id),
    KEY idx_comment_id (comment_id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子评论点赞表';



CREATE TABLE u_notification
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id      BIGINT      NOT NULL COMMENT '接收通知的用户 ID',
    from_user_id BIGINT      NOT NULL COMMENT '触发通知的用户 ID',
    type         VARCHAR(50) NOT NULL COMMENT '通知类型（LIKE、COMMENT 等）',
    content      TEXT COMMENT '通知内容',
    biz_id       BIGINT COMMENT '关联的业务 ID（如帖子 ID）',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT ='用户消息通知表';

ALTER TABLE u_notification
    ADD COLUMN is_online BOOLEAN DEFAULT FALSE COMMENT '发送通知时用户是否在线';



CREATE TABLE `u_class_schedule`
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    class_id     BIGINT       NOT NULL COMMENT '班级ID（关联学生的所属班级）',

    course_name  VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_name VARCHAR(100) NOT NULL COMMENT '任课老师名称',
    classroom    VARCHAR(100) DEFAULT NULL COMMENT '上课教室',

    day_of_week  TINYINT      NOT NULL COMMENT '星期几（1=周一，7=周日）',
    `period`     TINYINT      NOT NULL COMMENT '第几节课（如1表示第一节）',

    week_range   VARCHAR(50)  NOT NULL COMMENT '周次范围，如"1-16"、"1,3,5,7"',

    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT ='班级课表';


#题库分类
DROP TABLE IF EXISTS `u_question_category`;
CREATE TABLE `u_question_category`
(
    `id`          bigint                          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
    `parent_id`   int(11)                                  DEFAULT '0' COMMENT '父分类ID，0表示一级分类',
    `create_time` datetime                                 DEFAULT NULL COMMENT '创建时间',
    `is_deleted`  int(11)                         NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS `u_exam_repo`;
CREATE TABLE `u_exam_repo`
(
    `id`          bigint                           NOT NULL AUTO_INCREMENT COMMENT 'id   题库表',
    `user_id`     bigint                           NOT NULL COMMENT '创建人id',
    `title`       varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '题库标题',
    `category_id` bigint                                    DEFAULT NULL COMMENT '分类ID',
    `create_time` datetime                         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`  int(11)                          NOT NULL DEFAULT '0' COMMENT '逻辑删除：0代表未删除，1代表删除',
    `is_exercise` int(11)                          NOT NULL DEFAULT '0' comment '是否可以练习 默认为false',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;


CREATE TABLE `u_question` (
                              `id` BIGINT NOT NULL COMMENT '试题ID',
                              `repo_id` BIGINT NOT NULL COMMENT '所属题库ID',
                              `category_id` BIGINT NOT NULL COMMENT '分类ID',
                              `type` TINYINT NOT NULL COMMENT '题目类型：1单选 2多选 3判断 4简答',
                              `content` TEXT NOT NULL COMMENT '题目内容',
                              `correct_answer` VARCHAR(1000) NOT NULL COMMENT '正确答案（选择题为A,B,C,D）',
                              `difficulty` TINYINT DEFAULT 1 COMMENT '难度：1简单 2中等 3困难',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1删除',
                              PRIMARY KEY (`id`)
) COMMENT='试题表';

CREATE TABLE `u_question_option` (
                                     `id` BIGINT NOT NULL COMMENT '选项ID',
                                     `question_id` BIGINT NOT NULL COMMENT '所属试题ID',
                                     `option_key` CHAR(1) NOT NULL COMMENT '选项标识（如A/B/C/D）',
                                     `content` TEXT NOT NULL COMMENT '选项内容',
                                     `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1删除',
                                     PRIMARY KEY (`id`)
) COMMENT='试题选项表';

ALTER TABLE `u_question_option`
    ADD COLUMN `image` VARCHAR(500) DEFAULT NULL COMMENT '选项配图地址',
    ADD COLUMN `sort` INT DEFAULT 0 COMMENT '排序，值越小越靠前';



