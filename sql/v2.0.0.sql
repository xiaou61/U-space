-- 创建管理员用户表
CREATE TABLE `admin_user` (
                              `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID（UUID）',
                              `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                              `password` VARCHAR(100) NOT NULL COMMENT '密码'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='管理员用户表（UUID主键）';


CREATE TABLE `user_role` (
                             `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID（UUID）',
                             `role` VARCHAR(50) NOT NULL COMMENT '角色名称'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色表（UUID主键）';


CREATE TABLE `u_class` (
                           `id` VARCHAR(32) NOT NULL COMMENT '主键ID（UUID）',
                           `class_name` VARCHAR(100) NOT NULL COMMENT '班级名称',
                           `grade` VARCHAR(50) NOT NULL COMMENT '年级，2025级',
                           `major` VARCHAR(100) DEFAULT NULL COMMENT '所属专业，例如：计算机科学与技术',
                           `class_teacher` VARCHAR(100) DEFAULT NULL COMMENT '班主任姓名',
                           `student_count` INT UNSIGNED DEFAULT 0 COMMENT '学生人数',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

CREATE TABLE `u_teacher` (
                           `id` VARCHAR(32) NOT NULL COMMENT '主键UUID，32位无连字符',
                           `employee_no` CHAR(8) NOT NULL COMMENT '教职工编号（8位数字字符串）',
                           `name` VARCHAR(100) NOT NULL COMMENT '教师姓名',
                           `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                           `password` VARCHAR(64) NOT NULL COMMENT '密码，默认手机号后6位',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uniq_employee_no` (`employee_no`),
                           UNIQUE KEY `uniq_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

CREATE TABLE `u_student` (
                           `id` VARCHAR(32) NOT NULL COMMENT '学生ID，主键UUID（无连字符）',
                           `student_no` CHAR(10) NOT NULL COMMENT '学号（唯一编号）',
                           `name` VARCHAR(100) NOT NULL COMMENT '学生姓名',
                           `class_id` VARCHAR(32) NOT NULL COMMENT '班级ID，关联class表主键',
                           `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                           `password` VARCHAR(64) NOT NULL COMMENT '密码（默认学号或手机号后6位）',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uniq_student_no` (`student_no`),
                           UNIQUE KEY `uniq_phone` (`phone`),
                           KEY `idx_class_id` (`class_id`),
                           CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `u_class` (`id`)
                               ON UPDATE CASCADE
                               ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';


ALTER TABLE `u_student`
    ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-等待审核中，1-审核成功';

ALTER TABLE `u_student`
    ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址，支持URL';



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


CREATE TABLE `sys_announcement` (
                                    `id` VARCHAR(32) NOT NULL COMMENT '公告ID，UUID主键（无连字符）',
                                    `content` TEXT NOT NULL COMMENT '公告内容',
                                    `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '公告发布时间（默认当前时间）',
                                    `need_popup` TINYINT(1) DEFAULT 0 COMMENT '是否需要弹框提醒（0否，1是）',
                                    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除（0否，1是）',
                                    PRIMARY KEY (`id`)
) COMMENT='系统公告表';



CREATE TABLE `u_user_notify_message` (
                                     id           VARCHAR(32) PRIMARY KEY COMMENT '消息ID，UUID主键（无连字符）',
                                     user_id      VARCHAR(32) DEFAULT NULL COMMENT '接收者用户ID，为All表示广播公告',
                                     content      TEXT COMMENT '消息内容',
                                     type         VARCHAR(30) NOT NULL COMMENT '消息类型（如：system、like、comment）',
                                     scope        VARCHAR(10) NOT NULL COMMENT '作用范围：system=公告，private=个人',
                                     is_push      TINYINT DEFAULT 0 COMMENT '是否通过了SSE推送：0否，1是',
                                     create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户通知消息表';



CREATE TABLE `u_group` (
                           `id` VARCHAR(32) NOT NULL COMMENT '群组ID，UUID',
                           `name` VARCHAR(100) NOT NULL COMMENT '群组名称',
                           `description` TEXT DEFAULT NULL COMMENT '群组描述',
                           `creator_id` VARCHAR(32) NOT NULL COMMENT '创建人ID（老师）',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

CREATE TABLE `u_group_member` (
                                  `id` VARCHAR(32) NOT NULL COMMENT '主键UUID',
                                  `group_id` VARCHAR(32) NOT NULL COMMENT '群组ID',
                                  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID（学生ID）',
                                  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uniq_group_user` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';



CREATE TABLE `u_signin` (
                            `id` VARCHAR(32) NOT NULL COMMENT '签到任务ID，UUID',
                            `group_id` VARCHAR(32) NOT NULL COMMENT '群组ID，关联u_group表',
                            `creator_id` VARCHAR(32) NOT NULL COMMENT '发起人ID（教师ID）',
                            `type` TINYINT(1) NOT NULL COMMENT '签到类型：0=普通，1=密码，2=位置',
                            `password` VARCHAR(10) DEFAULT NULL COMMENT '签到密码（仅限密码签到）',
                            `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度（位置签到用）',
                            `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度（位置签到用）',
                            `location_radius` INT DEFAULT NULL COMMENT '允许的签到范围（单位：米）',
                            `end_time` DATETIME DEFAULT NULL COMMENT '签到截止时间',
                            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_group_id` (`group_id`),
                            KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组签到任务表';

CREATE TABLE `u_signin_record` (
                                   `id` VARCHAR(32) NOT NULL COMMENT '签到记录ID，UUID',
                                   `signin_id` VARCHAR(32) NOT NULL COMMENT '签到任务ID，关联u_signin表',
                                   `student_id` VARCHAR(32) NOT NULL COMMENT '签到人ID，关联u_student表',
                                   `type` TINYINT(1) NOT NULL COMMENT '签到类型：0=普通，1=密码，2=位置',
                                   `signin_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
                                   `is_late` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否迟到：0-未迟到，1-迟到',
                                   `password` VARCHAR(255) DEFAULT NULL COMMENT '签到棉麻',
                                   `latitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '签到纬度，仅位置签到时使用',
                                   `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '签到经度，仅位置签到时使用',
                                   `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '签到状态：0-无效，1-有效',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                   `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uniq_signin_user` (`signin_id`, `student_id`),
                                   KEY `idx_signin_id` (`signin_id`),
                                   KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';



CREATE TABLE `u_homework` (
                              `id` VARCHAR(32) PRIMARY KEY COMMENT '作业ID，主键，UUID格式',
                              `teacher_id` VARCHAR(32) NOT NULL COMMENT '发布作业的老师ID，关联教师表',
                              `group_id` VARCHAR(32) NOT NULL COMMENT '所属群组ID，关联群组表',
                              `title` VARCHAR(255) NOT NULL COMMENT '作业标题',
                              `description` TEXT COMMENT '作业描述，内容说明',
                              `deadline` DATETIME COMMENT '作业截止提交时间',
                              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间'
) COMMENT='作业表，存储老师发布的作业信息';


CREATE TABLE `u_homework_submission` (
                                         `id` VARCHAR(32) PRIMARY KEY COMMENT '提交ID，主键，UUID格式',
                                         `homework_id` VARCHAR(32) NOT NULL COMMENT '关联的作业ID，外键，关联u_homework表',
                                         `student_id` VARCHAR(32) NOT NULL COMMENT '提交作业的学生ID，关联学生表',
                                         `content` TEXT COMMENT '学生提交的文本内容',
                                         `attachment_urls` JSON COMMENT '附件地址的JSON数组，存储多个附件链接',
                                         `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间，默认当前时间',
                                         `status` VARCHAR(20) DEFAULT 'submitted' COMMENT '提交状态，如submitted(已提交), graded(已评分)等',
                                         `grade` DECIMAL(5,2) COMMENT '评分，最多5位，2位小数',
                                         `feedback` TEXT COMMENT '老师对作业的反馈意见',
                                         `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                         `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                                         CONSTRAINT `fk_homework_submission_homework` FOREIGN KEY (`homework_id`) REFERENCES `u_homework`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                         UNIQUE KEY `unique_homework_student` (`homework_id`, `student_id`)
) COMMENT='作业提交表，存储学生提交的作业内容及附件等信息';
