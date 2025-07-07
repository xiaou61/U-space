package com.xiaou.study.group.student.controller;

import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.resp.GroupMaterialResp;
import com.xiaou.study.group.teacher.service.GroupMaterialService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/material")
@Validated
public class GroupMateriaController {
    @Resource
    private GroupMaterialService groupMaterialService;
    /**
     * 查看当前群组的资料
     */
    @PostMapping("/list")
    public R<List<GroupMaterialResp>> list(@RequestParam String groupId){
        return groupMaterialService.listByMy(groupId);
    }
}
