package com.xiaou.system.log.service;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.system.log.domain.bo.SysOperLogBo;
import com.xiaou.system.log.domain.excel.SysOperLogExcelEntity;
import com.xiaou.system.log.domain.vo.SysOperLogVo;

import java.time.LocalDateTime;
import java.util.List;


public interface ISysOperLogService {
    R<PageRespDto<SysOperLogVo>> selectPageOperLogList(PageReqDto req);

    void insertOperlog(SysOperLogBo bo);


    List<SysOperLogExcelEntity> getExcelData(LocalDateTime beginTime, LocalDateTime endTime);
}
