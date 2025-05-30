package com.xiaou.userinfo.service;


import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.userinfo.domain.bo.UCollegeBO;
import com.xiaou.userinfo.domain.entity.College;
import com.xiaou.userinfo.domain.vo.UCollegeVO;

public interface CollegeService {
    R<UCollegeVO> addCollege(UCollegeBO collegeBO);

    R<UCollegeVO> updateCollege(Long id, UCollegeBO collegeBO);

    R<Void> deleteCollege(Long id);

    R<PageRespDto<College>> allCollegePage(PageReqDto pageReqDto);

    R<UCollegeVO> getCollegeById(Long id);
}
