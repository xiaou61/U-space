CREATE TABLE admin_user (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                            username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                            password VARCHAR(100) NOT NULL COMMENT '密码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';
