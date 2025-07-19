package com.xiaou.room.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.room.domain.req.DormBedReq;
import com.xiaou.room.domain.req.DormBuildingReq;
import com.xiaou.room.domain.req.DormRoomReq;
import com.xiaou.room.domain.resp.DormAllResp;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormBuildingResp;
import com.xiaou.room.domain.resp.DormRoomResp;
import com.xiaou.room.service.DormBedService;
import com.xiaou.room.service.DormBuildingService;
import com.xiaou.room.service.DormRoomService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dorm")
@Validated
/**
 * 宿舍相关的管理端接口
 */
public class DormAdminController {
    @Resource
    private DormBuildingService dormBuildingService;

    @Resource
    private DormRoomService dormRoomService;

    @Resource
    private DormBedService dormBedService;

    /**
     * 添加宿舍楼
     */
    @PostMapping("/addBuilding")
    public R<String> addBuilding(@RequestBody DormBuildingReq req) {
        return dormBuildingService.add(req);
    }
    /**
     * 添加宿舍楼里面的宿舍
     */
    @PostMapping("/addRoom")
    public R<String> addRoom(@RequestBody DormRoomReq req) {
        return dormRoomService.add(req);
    }
    /**
     * 添加宿舍的具体床铺
     */
    @PostMapping("/addBed")
    public R<String> addBed(@RequestBody DormBedReq req) {
        return dormBedService.add(req);
    }
    /**
     * 删除宿舍楼
     */
    @PostMapping("/removeBuilding")
    public R<String> removeBuilding(@RequestParam String id) {
        return dormBuildingService.removeBuilding(id);
    }
    /**
     * 删除宿舍楼里面的宿舍
     */
    @PostMapping("/removeRoom")
    public R<String> removeRoom(@RequestParam String id) {
        return dormRoomService.removeRoom(id);
    }
    /**
     * 删除宿舍楼里面的宿舍的床铺
     */
    @PostMapping("/removeBed")
    public R<String> removeBed(@RequestParam String id) {
        return dormBedService.removeBed(id);
    }
    /**
     * 列出所有宿舍楼
     */
    @GetMapping("/listBuilding")
    public R<List<DormBuildingResp>> listBuilding() {
        return dormBuildingService.listBuilding();
    }
    /**
     * 列出所有的宿舍房间 根据宿舍楼id
     */
    @GetMapping("/listRoom")
    public R<List<DormRoomResp>> listRoom(@RequestParam String buildingId) {
        return dormRoomService.listRoom(buildingId);
    }
    /**
     * 列出所有的床铺 根据宿舍房间id
     */
    @GetMapping("/listBed")
    public R<List<DormBedResp>> listBed(@RequestParam String roomId) {
        return dormBedService.listBed(roomId);
    }

    /**
     * 分页列出所有信息 废弃方法
     */
    @GetMapping("/list")
    @Deprecated
    public R<PageRespDto<DormAllResp>> listAll(@RequestBody PageReqDto req){
        return dormBuildingService.listAll(req);
    }
}
