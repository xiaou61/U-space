package com.xiaou.bbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.entity.BbsUserNotify;
import com.xiaou.bbs.domain.resp.BbsUserNotifyResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface BbsUserNotifyService extends IService<BbsUserNotify> {

    R<PageRespDto<BbsUserNotifyResp>> pageList(PageReqDto dto);
}
