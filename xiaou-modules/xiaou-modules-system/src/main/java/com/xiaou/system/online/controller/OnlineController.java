package com.xiaou.system.online.controller;

import com.xiaou.common.domain.R;
import com.xiaou.sse.core.SseEmitterManager;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController
@RequestMapping("/online")
public class OnlineController {
    @Resource
    private SseEmitterManager sseEmitterManager;
    /**
     * 统计在线人数
     */
    @GetMapping("/count")
    public R<Integer> count(){
        return R.ok(sseEmitterManager.getOnlineUserCount());
    }
    /**
     * 获取所有在线用户
     */
    @GetMapping("/users")
    public R<Set<String>> users(){
        return R.ok(sseEmitterManager.getOnlineUserIds());
    }
}
