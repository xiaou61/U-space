package com.xiaou.room.controller;

import com.xiaou.common.domain.R;
import com.xiaou.room.domain.req.DormRegisterReq;
import com.xiaou.room.domain.resp.ListMyResp;
import com.xiaou.room.service.DormRegisterService;
import com.xiaou.room.service.DormRoomService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dorm")
@Validated
public class DormController {
    @Resource
    private DormRegisterService dormRegisterService;


    @Resource
    private DormRoomService dormRoomService;
    /**
     * 判断用户是否需要输入信息
     * false为不需要 true为需要
     */
    @GetMapping("/isNeedInputInfo")
    public R<Boolean> isNeedInputInfo() {
        return dormRegisterService.isNeedInputInfo();
    }
    /**
     * 输入信息
     */
    @PostMapping("/inputInfo")
    public R<String> inputInfo(@RequestBody DormRegisterReq req) {
        return dormRegisterService.inputInfo(req);
    }

    /**
     * 列出自己可选择的宿舍信息 聚合 这里要根据id获取性别然后返回的是自己的性别 room跟bed一个集合
     */
    @GetMapping("/list")
    public R<List<ListMyResp>> list() {
        return dormRoomService.listMy();
    }


    /**
     * 抢宿舍按钮 具体某个床铺
     * id为床铺id
     */
    @PostMapping("/grab")
    public R<String> grab(@RequestParam String roomId,@RequestParam String bedId) {
        return dormRegisterService.grab(roomId,bedId);
    }


}
