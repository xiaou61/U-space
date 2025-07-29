package com.xiaou.study.group.teacher.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.domain.entity.StudentEntity;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.Group;
import com.xiaou.study.group.teacher.domain.entity.GroupMember;
import com.xiaou.study.group.teacher.domain.req.GroupReq;
import com.xiaou.study.group.teacher.domain.resp.GroupMemberResp;
import com.xiaou.study.group.teacher.mapper.GroupMapper;
import com.xiaou.study.group.teacher.mapper.GroupMemberMapper;
import com.xiaou.study.group.teacher.service.GroupService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
        implements GroupService {

    @Resource
    private GroupMapper groupMapper;
    @Resource
    private LoginHelper loginHelper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private GroupMemberMapper groupMemberMapper;

    @Override
    public R<String> add(GroupReq req) {
        Group group = MapstructUtils.convert(req, Group.class);
        group.setCreatorId(loginHelper.getCurrentAppUserId());
        groupMapper.insert(group);
        return R.ok("成功");
    }

    @Override
    public R<String> delete(String id) {
        Group group = groupMapper.selectById(id);

        if (group == null) {
            return R.fail("群组不存在或已被删除");
        }

        String currentUserId = loginHelper.getCurrentAppUserId();
        if (!currentUserId.equals(group.getCreatorId())) {
            return R.fail("该群组不属于你，无法解散");
        }

        groupMapper.deleteById(id);
        return R.ok("解散成功");
    }

    @Override
    public R<List<Group>> listById() {
        //查询所有查找到自己的群组
        QueryWrapper<Group> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator_id", loginHelper.getCurrentAppUserId());
        return R.ok(groupMapper.selectList(queryWrapper));
    }

    @Override
    public R<String> generateId(String groupId) {
        String id = RandomStringUtils.random(8, true, true);
        String redisKey = GlobalConstants.GROUPCHECK_KEY + id;

        // 尝试设置，防止 ID 冲突
        boolean success = RedisUtils.setIfAbsent(redisKey, groupId, 1, TimeUnit.DAYS);
        if (!success) {
            return generateId(groupId);
        }

        return R.ok(id);
    }

    @Override
    public R<List<GroupMemberResp>> member(String groupId) {
        // 查出所有 groupMember 记录
        List<GroupMember> groupMembers = groupMemberMapper.selectList(
                new QueryWrapper<GroupMember>().eq("group_id", groupId)
        );

        // 拿出所有 userId
        List<String> userIds = groupMembers.stream()
                .map(GroupMember::getUserId)
                .toList();

        // 查出所有 student，并构建 userId -> userName 映射
        Map<String, String> userIdNameMap = studentMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(StudentEntity::getId, StudentEntity::getName));

        // 映射为响应对象并填充 userName
        List<GroupMemberResp> result = groupMembers.stream()
                .map(member -> {
                    GroupMemberResp resp = MapstructUtils.convert(member, GroupMemberResp.class);
                    resp.setUserName(userIdNameMap.get(member.getUserId()));
                    return resp;
                })
                .toList();

        return R.ok(result);
    }

}




