//package com.xiaou.system.online.controller;
//
//import com.xiaou.common.domain.R;
//import com.xiaou.online.manager.OnlineUserManager;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 目前已废弃。
// */
//@Deprecated
//@RestController
//@RequestMapping("/old/online")
//public class OnlineControllerV1 {
//
//
//
//    /**
//     * 获取所有在线用户 ID
//     */
//    @GetMapping("/list")
//    public R<List<String>> getOnlineUserList() {
//        return R.ok(OnlineUserManager.getAllOnlineUserIds());
//    }
//
//    /**
//     * 判断指定用户是否在线
//     */
//    @GetMapping("/check/{userId}")
//    public R<Boolean> isOnline(@PathVariable String userId) {
//        return R.ok(OnlineUserManager.isOnline(userId));
//    }
//
//    /**
//     * 手动标记下线
//     */
//    @PostMapping("/offline/{userId}")
//    public R<Void> forceOffline(@PathVariable String userId) {
//        OnlineUserManager.markOffline(userId);
//        return R.ok();
//    }
//
//}
