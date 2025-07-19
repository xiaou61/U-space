package com.xiaou.room.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 学生抢宿舍记录表
 * @TableName u_user_dorm_select
 */
@TableName(value ="u_user_dorm_select")
@Data
public class UserDormSelect {
    /**
     * 主键ID（UUID或雪花ID）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 学生用户ID（预留与用户表关联）
     */
    private String userId;

    /**
     * 抢到的宿舍ID
     */
    private String roomId;

    /**
     * 抢到的床位ID（如支持选床位）
     */
    private String bedId;

    /**
     * 状态（0-待确认或默认，1-已确认）
     */
    private Integer status;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}