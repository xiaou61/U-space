package com.xiaou.room.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.room.domain.entity.DormBuilding;
import com.xiaou.room.domain.req.DormBuildingReq;
import com.xiaou.room.domain.resp.DormAllResp;
import com.xiaou.room.domain.resp.DormBuildingResp;

import java.util.List;

public interface DormBuildingService extends IService<DormBuilding> {

    R<String> add(DormBuildingReq req);

    R<String> removeBuilding(String id);

    R<List<DormBuildingResp>> listBuilding();

    R<PageRespDto<DormAllResp>> listAll(PageReqDto req);
}
