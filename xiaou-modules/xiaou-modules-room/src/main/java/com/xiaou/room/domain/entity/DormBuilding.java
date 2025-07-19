package com.xiaou.room.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 宿舍楼表
 * @TableName u_dorm_building
 */
@TableName(value ="u_dorm_building")
@Data
public class DormBuilding {
    /**
     * 宿舍楼ID（主键）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 宿舍楼名称，如"一栋A座"
     */
    private String name;

    /**
     * 备注信息，如地理位置、楼层数等
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}