package com.xiaou.controller;

import com.xiaou.common.domain.R;
import com.xiaou.domain.bo.VoteCreateRequest;
import com.xiaou.service.VoteService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Deprecated
//todo 投票功能，咱不开发
@RestController
@RequestMapping("/user/votes")
@Validated
public class VoteController {
    @Resource
    private VoteService voteService;
    @PostMapping
    public R<Long> createVote(@Validated @RequestBody VoteCreateRequest request) {
        Long voteId = voteService.createVote(request);
        return R.ok(voteId);
    }

}
