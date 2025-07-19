package com.xiaou.room.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.room.domain.entity.DormRoom;
import com.xiaou.room.domain.req.DormRoomReq;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormBuildingResp;
import com.xiaou.room.domain.resp.DormRoomResp;
import com.xiaou.room.domain.resp.ListMyResp;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DormRoomService extends IService<DormRoom> {

    R<String> add(DormRoomReq req);

    R<String> removeRoom(String id);

    R<List<DormRoomResp>> listRoom(String buildingId);

    List<DormRoomResp> listDormRoomByBuildingIds(List<String> collect);

    R<List<ListMyResp>> listMy();
}
