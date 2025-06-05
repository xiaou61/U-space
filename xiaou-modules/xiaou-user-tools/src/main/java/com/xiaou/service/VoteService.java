package com.xiaou.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.domain.bo.VoteCreateRequest;
import com.xiaou.domain.entity.Vote;


public interface VoteService extends IService<Vote> {

    Long createVote(VoteCreateRequest request);
}
