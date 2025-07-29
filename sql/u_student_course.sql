-- 学生选课记录表
CREATE TABLE `u_student_course` (
  `id` varchar(32) NOT NULL COMMENT '记录ID',
  `student_id` varchar(32) NOT NULL COMMENT '学生ID',
  `course_id` varchar(32) NOT NULL COMMENT '课程ID',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '选课状态 0-已选 1-已退课',
  `select_time` datetime DEFAULT NULL COMMENT '选课时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '退课时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course` (`student_id`, `course_id`, `status`) COMMENT '学生课程唯一索引',
  KEY `idx_student_id` (`student_id`) COMMENT '学生ID索引',
  KEY `idx_course_id` (`course_id`) COMMENT '课程ID索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_select_time` (`select_time`) COMMENT '选课时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生选课记录表';

-- 为课程表添加索引优化（如果还没有）
ALTER TABLE `u_course` 
ADD INDEX `idx_capacity_selected` (`capacity`, `selected_count`) COMMENT '容量和已选人数联合索引';
