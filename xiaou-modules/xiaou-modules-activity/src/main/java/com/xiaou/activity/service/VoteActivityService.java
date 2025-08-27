package com.xiaou.activity.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.activity.domain.entity.VoteActivity;
import com.xiaou.activity.domain.req.VoteActivityReq;
import com.xiaou.activity.domain.resp.VoteActivityResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

public interface VoteActivityService extends IService<VoteActivity> {

    R<String> add(VoteActivityReq req);

    R<String> offline(String id);

    R<PageRespDto<VoteActivityResp>> listPage(PageReqDto reqDto);
}
