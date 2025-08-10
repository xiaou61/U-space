package com.xiaou.activity.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.activity.domain.entity.VoteOption;
import com.xiaou.activity.domain.req.VoteOptionReq;
import com.xiaou.common.domain.R;

public interface VoteOptionService extends IService<VoteOption> {

    R<String> addOption(String activityId, VoteOptionReq req);

    R<String> deleteOption(String optionId);
}
