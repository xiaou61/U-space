package com.xiaou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.domain.bo.VoteCreateRequest;
import com.xiaou.domain.entity.Vote;
import com.xiaou.domain.entity.VoteOption;
import com.xiaou.mapper.VoteMapper;
import com.xiaou.mapper.VoteOptionMapper;
import com.xiaou.service.VoteService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote>
        implements VoteService {
    @Resource
    private VoteOptionMapper voteOptionMapper;

    @Override
    public Long createVote(VoteCreateRequest request) {
        Vote vote = MapstructUtils.convert(request, Vote.class);
        vote.setCreatorId(LoginHelper.getCurrentAppUserId());
        vote.setStatus(1);

        baseMapper.insert(vote);
        for (String optionContent : request.getOptions()) {
            VoteOption option = new VoteOption();
            option.setVoteId(vote.getId());
            option.setContent(optionContent);
            voteOptionMapper.insert(option);
        }


        return vote.getId();
    }
}




