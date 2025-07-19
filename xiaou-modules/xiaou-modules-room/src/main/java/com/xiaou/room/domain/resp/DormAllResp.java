package com.xiaou.room.domain.resp;

import lombok.Data;

import java.util.List;
@Data
@Deprecated
public class DormAllResp {

    private List<DormBuildingResp> dormBuildings;

    private List<DormRoomResp> dormRooms;

    private List<DormBedResp> dormBeds;
}
