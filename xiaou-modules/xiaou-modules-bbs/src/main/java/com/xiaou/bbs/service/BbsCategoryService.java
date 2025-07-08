package com.xiaou.bbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.entity.BbsCategory;
import com.xiaou.bbs.domain.req.BbsCategoryReq;
import com.xiaou.bbs.domain.resp.BbsCategoryResp;
import com.xiaou.common.domain.R;

import java.util.List;

public interface BbsCategoryService extends IService<BbsCategory> {

    R<String> addCategory(BbsCategoryReq req);

    R<String> deleteCategory(String id);

    R<String> updateCategory(String id ,BbsCategoryReq req);

    R<List<BbsCategoryResp>> listCategory();
}
