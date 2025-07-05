package com.xiaou.study.group.teacher.handler;

import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.domain.req.SigninReq;

public interface SigninHandler {
    boolean support(Integer type);

    /**
     * 发布签到
     * @param req
     */
    void handle(SigninReq req);

    /**
     * 执行签到
     */
    void executeSignin(SigninRecordReq req);
}
