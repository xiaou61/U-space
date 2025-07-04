package com.xiaou.study.group.teacher.serivce.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.student.domain.resp.GroupResp;
import com.xiaou.study.group.teacher.domain.entity.Group;
import com.xiaou.study.group.teacher.domain.entity.GroupMember;
import com.xiaou.study.group.teacher.mapper.GroupMapper;
import com.xiaou.study.group.teacher.mapper.GroupMemberMapper;
import com.xiaou.study.group.teacher.serivce.GroupMemberService;
import com.xiaou.study.group.teacher.serivce.GroupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember>
        implements GroupMemberService {
    @Resource
    private GroupMemberMapper groupMemberMapper;

    @Resource
    private LoginHelper loginHelper;

    @Resource
    private GroupMapper groupMapper;

    @Override
    public R<String> join(String id) {
        //通过id找到群组
        String groupId = RedisUtils.getGroupIdByGeneratedId(id);
        if (groupId == null) {
            return R.fail("群组不存在");
        }
        //否则的话这个群组就一定存在
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setUserId(loginHelper.getCurrentAppUserId());
        groupMemberMapper.insert(groupMember);
        return R.ok("加入群组成功");
    }

    @Override
    public R<List<GroupResp>> listByMy() {
        String userId = loginHelper.getCurrentAppUserId();
        //通过userId查询到用户所在的所有groupId
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<GroupMember> groupMembers = groupMemberMapper.selectList(queryWrapper);
        List<String> groupIds = groupMembers.stream().map(GroupMember::getGroupId).toList();
        //如果groupIDs为空直接返回空
        if (groupIds.isEmpty()) {
            return R.ok(Collections.emptyList());
        }
        //根据groupIDs查询group表
        List<Group> groups = groupMapper.selectBatchIds(groupIds);

        return R.ok(MapstructUtils.convert(groups, GroupResp.class));
    }


}




