package com.xiaou.activity.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.activity.domain.entity.VoteActivity;
import com.xiaou.activity.domain.entity.VoteOption;
import com.xiaou.activity.domain.req.VoteActivityReq;
import com.xiaou.activity.domain.resp.VoteActivityResp;
import com.xiaou.activity.mapper.VoteActivityMapper;
import com.xiaou.activity.mapper.VoteOptionMapper;
import com.xiaou.activity.service.VoteActivityService;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.QueryWrapperUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VoteActivityServiceImpl extends ServiceImpl<VoteActivityMapper, VoteActivity>
        implements VoteActivityService {

    @Resource
    private VoteOptionMapper voteOptionMapper;

    @Override
    public R<String> add(VoteActivityReq req) {
        // 判断新增还是更新
        if (req.getId() != null) {
            // 更新操作
            VoteActivity voteActivity = baseMapper.selectById(req.getId());
            if (voteActivity == null) {
                return R.fail("活动不存在");
            }
            VoteActivity activity = BeanUtil.copyProperties(req, VoteActivity.class);
            baseMapper.updateById(activity);
            return R.ok("更新成功");
        } else {
            // 新增操作
            VoteActivity voteActivity = BeanUtil.copyProperties(req, VoteActivity.class);
            baseMapper.insert(voteActivity);
            return R.ok("添加成功");
        }
    }

    @Override
    public R<String> offline(String id) {
        VoteActivity voteActivity = baseMapper.selectById(id);
        if (voteActivity == null) {
            return R.fail("活动不存在");
        }
        voteActivity.setStatus(GlobalConstants.ZERO);
        baseMapper.updateById(voteActivity);
        return R.ok("下线成功");
    }

    @Override
    public R<PageRespDto<VoteActivityResp>> listPage(PageReqDto reqDto) {
        IPage<VoteActivity> page = new Page<>(reqDto.getPageNum(), reqDto.getPageSize());
        QueryWrapper<VoteActivity> queryWrapper = new QueryWrapper<>();
        IPage<VoteActivity> voteActivityIPage = baseMapper.selectPage(page, queryWrapper);
        List<VoteActivity> records = voteActivityIPage.getRecords();
        
        // 转换为响应对象
        List<VoteActivityResp> voteActivityResps = BeanUtil.copyToList(records, VoteActivityResp.class);
        
        // 批量查询优化：避免n+1查询问题
        if (!voteActivityResps.isEmpty()) {
            // 收集所有活动ID
            List<String> activityIds = voteActivityResps.stream()
                    .map(VoteActivityResp::getId)
                    .toList();
            
            // 一次性查询所有相关的投票选项
            List<VoteOption> allVoteOptions = voteOptionMapper.selectList(
                    new QueryWrapper<VoteOption>().in("activity_id", activityIds)
            );
            
            // 按活动ID分组
            Map<String, List<VoteOption>> optionsMap = allVoteOptions.stream()
                    .collect(Collectors.groupingBy(VoteOption::getActivityId));
            
            // 填充数据
            voteActivityResps.forEach(resp -> 
                resp.setOptions(optionsMap.getOrDefault(resp.getId(), Collections.emptyList()))
            );
        }

        return R.ok(PageRespDto.of(reqDto.getPageNum(), reqDto.getPageSize(),
                voteActivityIPage.getTotal(),
                voteActivityResps
        ));
    }

}




