-- 宿舍房间表，用于记录每个宿舍的基本信息（例如房号、容量、剩余床位等）
CREATE TABLE `u_dorm_room` (
                               id VARCHAR(32) PRIMARY KEY COMMENT '宿舍ID（主键，UUID或雪花ID）',

                               building_id VARCHAR(32) DEFAULT NULL COMMENT '宿舍楼ID',

                               room_number VARCHAR(20) NOT NULL COMMENT '宿舍房间号，如A101、B302',

                               capacity INT DEFAULT 4 COMMENT '宿舍最大可容纳人数（床位总数）',

                               available INT DEFAULT 4 COMMENT '当前剩余可选床位数（缓存到Redis中做库存控制）',

                               gender TINYINT DEFAULT 0 COMMENT '性别限制1-男生宿舍，2-女生宿舍',

                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) COMMENT='宿舍房间表';
-- 宿舍床位表，用于精细化到每个宿舍的床位（如上铺、下铺），支持具体床位选择
CREATE TABLE `u_dorm_bed` (
                              id VARCHAR(32) PRIMARY KEY COMMENT '床位ID（主键，UUID或雪花ID）',

                              room_id VARCHAR(32) NOT NULL COMMENT '所属宿舍ID，关联 dorm_room 表',

                              bed_number VARCHAR(10) NOT NULL COMMENT '床位编号，如"1号床"',

                              status TINYINT DEFAULT 0 COMMENT '床位状态（0-未被选择，1-已被抢占）',

                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='宿舍床位表';
-- 学生抢宿舍记录表，用于记录每个学生的宿舍选择结果
CREATE TABLE `u_user_dorm_select` (
                                      id VARCHAR(32) PRIMARY KEY COMMENT '主键ID（UUID或雪花ID）',

                                      user_id VARCHAR(32) NOT NULL COMMENT '学生用户ID（预留与用户表关联）',

                                      room_id VARCHAR(32) NOT NULL COMMENT '抢到的宿舍ID',

                                      bed_id VARCHAR(32) DEFAULT NULL COMMENT '抢到的床位ID（如支持选床位）',

                                      status TINYINT DEFAULT 0 COMMENT '状态（0-待确认或默认，1-已确认）',

                                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
) COMMENT='学生抢宿舍记录表';
-- 宿舍楼表，用于记录宿舍楼的基本信息，支持分栋管理
CREATE TABLE `u_dorm_building` (
                                   id VARCHAR(32) PRIMARY KEY COMMENT '宿舍楼ID（主键）',

                                   name VARCHAR(50) NOT NULL COMMENT '宿舍楼名称，如"一栋A座"',

                                   remark VARCHAR(255) DEFAULT NULL COMMENT '备注信息，如地理位置、楼层数等',

                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) COMMENT='宿舍楼表';

-- 宿舍登记表：记录用户登记宿舍的信息（包括性别、身份证等）
CREATE TABLE `u_dorm_register` (
                                   id VARCHAR(32) PRIMARY KEY COMMENT '主键ID（UUID或雪花ID）',

                                   user_id VARCHAR(32) NOT NULL COMMENT '用户学号或唯一账号ID',

                                   gender TINYINT DEFAULT 0 COMMENT '性别（0-未知，1-男，2-女）',

                                   id_card VARCHAR(18) NOT NULL COMMENT '身份证号码（支持合法性校验）',

                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',

                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    -- 唯一约束：用户不能重复登记
                                   UNIQUE KEY uk_user_id (user_id),

    -- 唯一约束：身份证号码唯一（防止重复身份）
                                   UNIQUE KEY uk_id_card (id_card)
) COMMENT='宿舍信息登记表';