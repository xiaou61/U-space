package com.xiaou.room.domain.req;

import lombok.Data;

@Data
public class DormBedReq {
    /**
     * 所属宿舍ID，关联 dorm_room 表
     */
    private String roomId;

    /**
     * 床位编号，如"1号床"
     */
    private String bedNumber;
}
