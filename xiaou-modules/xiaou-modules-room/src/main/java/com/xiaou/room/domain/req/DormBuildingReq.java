package com.xiaou.room.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.room.domain.entity.DormBuilding;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 宿舍楼表
 * @TableName u_dorm_building
 */
@Data
@AutoMapper(target = DormBuilding.class)
public class DormBuildingReq {

    /**
     * 宿舍楼名称，如"一栋A座"
     */
    private String name;

    /**
     * 备注信息，如地理位置、楼层数等
     */
    private String remark;

}