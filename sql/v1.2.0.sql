#xiaou-modules-grade模块
CREATE TABLE u_teacher (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
                         name VARCHAR(50) NOT NULL COMMENT '教师姓名',
                         phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号（用于登录）',
                         password VARCHAR(100) NOT NULL COMMENT '密码（默认为手机号后六位）',
                         status TINYINT DEFAULT 1 COMMENT '状态（1-正常，0-禁用）',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
CREATE TABLE u_classroom (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
                           name VARCHAR(100) NOT NULL COMMENT '班级名称',
                           code VARCHAR(10) UNIQUE NOT NULL COMMENT '班级邀请码（扫码/手动输入用）',
                           teacher_id BIGINT NOT NULL COMMENT '创建者教师ID',
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           FOREIGN KEY (teacher_id) REFERENCES u_teacher(id)
);
CREATE TABLE u_student_classroom (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                   student_id BIGINT NOT NULL COMMENT '学生ID',
                                   classroom_id BIGINT NOT NULL COMMENT '班级ID',
                                   joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                   UNIQUE (student_id, classroom_id),
                                   FOREIGN KEY (classroom_id) REFERENCES u_classroom(id)
);
