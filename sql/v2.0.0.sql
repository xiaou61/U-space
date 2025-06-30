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