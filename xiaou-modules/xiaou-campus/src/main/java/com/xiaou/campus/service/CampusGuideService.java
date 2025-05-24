package com.xiaou.campus.service;

import com.xiaou.campus.domain.bo.CampusGuideBO;
import com.xiaou.campus.domain.vo.CampusGuideVO;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface CampusGuideService {
    R<CampusGuideVO> addGuide(CampusGuideBO campusGuideBO);

    R<CampusGuideVO> updateGuide(Long id, CampusGuideBO campusGuideBO);

    R<String> deleteGuide(Long id);

    R<PageRespDto<CampusGuideVO>> allGuidePage(PageReqDto dto);
}
