package com.xiaou.study.group.teacher.serivce;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;

public interface SigninService extends IService<Signin> {

    R<String> add(SigninReq req);

    R<PageRespDto<SigninResp>> PageList(PageReqDto req);
}
