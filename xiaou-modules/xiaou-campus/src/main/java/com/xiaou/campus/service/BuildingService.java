package com.xiaou.campus.service;

import com.xiaou.campus.domain.bo.BuildingInfoBO;
import com.xiaou.campus.domain.bo.CampusGuideBO;
import com.xiaou.campus.domain.vo.BuildingInfoVO;
import com.xiaou.campus.domain.vo.CampusGuideVO;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface BuildingService {
    R<BuildingInfoVO> addBuild(BuildingInfoBO bo);

    R<BuildingInfoVO> updateBuild(Integer id, BuildingInfoBO buildingInfoBO);

    R<String> deleteBuild(Integer id);

    R<PageRespDto<BuildingInfoVO>> allGuidePage(PageReqDto pageReqDto);
}
