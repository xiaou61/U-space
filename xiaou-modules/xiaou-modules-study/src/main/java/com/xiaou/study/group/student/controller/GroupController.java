package com.xiaou.study.group.student.controller;

import com.xiaou.common.domain.R;
import com.xiaou.ratelimiter.annotation.RateLimiter;
import com.xiaou.study.group.student.domain.resp.GroupResp;
import com.xiaou.study.group.teacher.service.GroupMemberService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/group")
@Validated
public class GroupController {
    @Resource
    private GroupMemberService groupMemberService;

    /**
     * 加入一个群组
     */
    @RateLimiter(key = "joinGroup", time = 60, count = 5)
    @PostMapping("/join")
    public R<String> join(@RequestParam String id) {
        return groupMemberService.join(id);
    }

    /**
     * 查看自己所在的群组
     */
    @PostMapping("/list")
    public R<List<GroupResp>> list() {
        return groupMemberService.listByMy();
    }
}
