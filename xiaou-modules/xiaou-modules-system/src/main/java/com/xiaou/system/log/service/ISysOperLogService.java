package com.xiaou.system.log.service;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.system.log.domain.bo.SysOperLogBo;
import com.xiaou.system.log.domain.vo.SysOperLogVo;


public interface ISysOperLogService {
    R<PageRespDto<SysOperLogVo>> selectPageOperLogList(PageReqDto req);

    void insertOperlog(SysOperLogBo bo);
}
