package com.xiaou.campus.service;

import com.xiaou.campus.domain.bo.PlaceCategoryBO;
import com.xiaou.campus.domain.vo.PlaceCategoryVO;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface PlaceCategoryService {
    R<PlaceCategoryVO> addCategory(PlaceCategoryBO bo);

    R<Boolean> deleteCategory(Long id);

    R<PlaceCategoryVO> updateCategory(Long id, PlaceCategoryBO bo);

    R<PageRespDto<PlaceCategoryVO>> list(PageReqDto req);
}
