package com.xiaou.room.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.room.domain.entity.DormBed;
import com.xiaou.room.domain.req.DormBedReq;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormBuildingResp;
import com.xiaou.room.domain.resp.DormRoomResp;

import java.util.List;

public interface DormBedService extends IService<DormBed> {

    R<String> add(DormBedReq req);

    R<String> removeBed(String id);

    R<List<DormBedResp>> listBed(String roomId);

    List<DormBedResp> listDormBedByRoomIds(List<String> collect);
}
