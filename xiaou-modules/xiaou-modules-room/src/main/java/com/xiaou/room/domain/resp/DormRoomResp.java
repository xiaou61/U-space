package com.xiaou.room.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.room.domain.entity.DormRoom;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 宿舍房间表
 * @TableName u_dorm_room
 */
@Data
@AutoMapper(target = DormRoom.class)
public class DormRoomResp {
    /**
     * 宿舍ID（主键，UUID或雪花ID）
     */
    private String id;

    /**
     * 宿舍房间号，如A101、B302
     */
    private String roomNumber;

    /**
     * 宿舍最大可容纳人数（床位总数）
     */
    private Integer capacity;

    /**
     * 当前剩余可选床位数（缓存到Redis中做库存控制）
     */
    private Integer available;

    /**
     * 性别限制1-男生宿舍，2-女生宿舍
     */
    private Integer gender;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;
}