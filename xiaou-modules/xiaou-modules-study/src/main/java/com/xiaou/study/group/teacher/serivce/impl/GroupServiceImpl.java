package com.xiaou.study.group.teacher.serivce.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.Group;
import com.xiaou.study.group.teacher.domain.req.GroupReq;
import com.xiaou.study.group.teacher.mapper.GroupMapper;
import com.xiaou.study.group.teacher.serivce.GroupService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
        implements GroupService {

    @Resource
    private GroupMapper groupMapper;
    @Resource
    private LoginHelper loginHelper;

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
}




