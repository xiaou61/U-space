package com.xiaou.study.group.teacher.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.GroupMaterial;
import com.xiaou.study.group.teacher.domain.req.GroupMaterialReq;
import com.xiaou.study.group.teacher.domain.resp.GroupMaterialResp;
import com.xiaou.study.group.teacher.mapper.GroupMapper;
import com.xiaou.study.group.teacher.mapper.GroupMaterialMapper;
import com.xiaou.study.group.teacher.service.GroupMaterialService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMaterialServiceImpl extends ServiceImpl<GroupMaterialMapper, GroupMaterial>
    implements GroupMaterialService {

    @Resource
    private LoginHelper loginHelper;
    @Resource
    private GroupMapper groupMapper;
    @Override
    public R<String> add(GroupMaterialReq req) {
        GroupMaterial convert = MapstructUtils.convert(req, GroupMaterial.class);
        convert.setUploaderId(loginHelper.getCurrentAppUserId());
        baseMapper.insert(convert);
        return R.ok("添加成功");
    }

    @Override
    public R<String> delete(String id) {
        //判断老师是否有这个权限
        if (!loginHelper.getCurrentAppUserId().equals(baseMapper.selectById(id).getUploaderId())) {
            return R.fail("这个不是你的群组无法删除");
        }
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<PageRespDto<GroupMaterialResp>> listPage(PageReqDto req) {
        IPage<GroupMaterial> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<GroupMaterial> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, req, List.of(req.getSortField()));
        IPage<GroupMaterial> groupMaterialIPage = baseMapper.selectPage(page, queryWrapper);
        //添加GroupName
        List<GroupMaterialResp> resps = MapstructUtils.convert(groupMaterialIPage.getRecords(), GroupMaterialResp.class);
        resps.forEach(item -> item.setGroupName(groupMapper.selectById(item.getGroupId()).getName()));
        return R.ok(PageRespDto.of(req.getPageNum(),req.getPageSize(),groupMaterialIPage.getTotal(),resps));
    }

    @Override
    public R<List<GroupMaterialResp>> listByMy(String groupId) {
        QueryWrapper<GroupMaterial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        List<GroupMaterial> groupMaterials = baseMapper.selectList(queryWrapper);
        List<GroupMaterialResp> resps = MapstructUtils.convert(groupMaterials, GroupMaterialResp.class);
        return R.ok(resps);
    }
}




