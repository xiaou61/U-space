package com.xiaou.study.group.teacher.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.study.group.teacher.domain.entity.Group;
import com.xiaou.study.group.teacher.domain.req.GroupReq;
import com.xiaou.study.group.teacher.serivce.GroupService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/group")
@Validated
public class GroupAdminController {
    @Resource
    private GroupService groupService;

    /**
     * 老师添加一个群组
     */
    @PostMapping("/add")
    @SaCheckRole(RoleConstant.TEACHER)
    public R<String> add(@RequestBody GroupReq req) {
        return groupService.add(req);
    }

    /**
     * 解散一个群组
     */

    @PostMapping("/delete")
    @SaCheckRole(RoleConstant.TEACHER)
    public R<String> delete(@RequestParam String id) {
        return groupService.delete(id);
    }
    /**
     * 查看自己的群组
     */
    @PostMapping("/list")
    @SaCheckRole(RoleConstant.TEACHER)
    public R<List<Group>> list() {
        return groupService.listById();
    }
    /**
     * 生成一个群组的数字id
     */
    @PostMapping("/generateId")
    @SaCheckRole(RoleConstant.TEACHER)
    public R<String> generateId(@RequestParam String groupId) {
        return groupService.generateId(groupId);
    }

}
