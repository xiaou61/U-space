package com.xiaou.study.group.teacher.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.study.group.teacher.domain.entity.GroupMaterial;
import com.xiaou.study.group.teacher.domain.req.GroupMaterialReq;
import com.xiaou.study.group.teacher.domain.resp.GroupMaterialResp;
import com.xiaou.study.group.teacher.service.GroupMaterialService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher/material")
@Validated
public class GroupMaterialAdminController {

    @Resource
    private GroupMaterialService groupMaterialService;
    /**
     * 添加一个资料
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody GroupMaterialReq req){
        return groupMaterialService.add(req);
    }
    /**
     * 删除一个资料
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestParam String id){
        return groupMaterialService.delete(id);
    }
    /**
     * 查看所有上传的资料分页
     */
    @PostMapping("/listpage")
    public R<PageRespDto<GroupMaterialResp>> listPage(@RequestBody PageReqDto req){
        return groupMaterialService.listPage(req);
    }

}
