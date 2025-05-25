package com.xiaou.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.campus.domain.entity.QaPairs;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface QaPairsService extends IService<QaPairs> {

    R<QaPairs> addQA(QaPairs qaPairs);

    R<QaPairs> deleteQA(Long id);

    R<QaPairs> updateQA(QaPairs qaPairs);

    R<PageRespDto<QaPairs>> allQaPage(PageReqDto dto);
}