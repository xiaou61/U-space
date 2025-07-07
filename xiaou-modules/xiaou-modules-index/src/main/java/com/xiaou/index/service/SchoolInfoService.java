package com.xiaou.index.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.index.domain.entity.SchoolInfo;
import com.xiaou.index.domain.req.SchoolInfoReq;
import com.xiaou.index.domain.resp.SchoolInfoResp;

public interface SchoolInfoService extends IService<SchoolInfo> {

    R<String > updateSchoolInfo(SchoolInfoReq req);

    R<SchoolInfoResp> info();
}
