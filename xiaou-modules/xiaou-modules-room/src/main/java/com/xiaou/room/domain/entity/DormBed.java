package com.xiaou.room.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 宿舍床位表
 * @TableName u_dorm_bed
 */
@TableName(value ="u_dorm_bed")
@Data
public class DormBed {
    /**
     * 床位ID（主键，UUID或雪花ID）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属宿舍ID，关联 dorm_room 表
     */
    private String roomId;

    /**
     * 床位编号，如"1号床"
     */
    private String bedNumber;

    /**
     * 床位状态（0-未被选择，1-已被抢占）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}