package com.xiaou.room.domain.req;

import lombok.Data;

/**
 * 宿舍楼表
 * @TableName u_dorm_building
 */
@Data
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