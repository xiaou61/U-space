package com.xiaou.activity.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.activity.domain.entity.VoteOption;
import com.xiaou.activity.domain.req.VoteOptionReq;
import com.xiaou.activity.mapper.VoteOptionMapper;
import com.xiaou.activity.service.VoteOptionService;
import com.xiaou.common.domain.R;
import org.springframework.stereotype.Service;

@Service
public class VoteOptionServiceImpl extends ServiceImpl<VoteOptionMapper, VoteOption>
    implements VoteOptionService {

    @Override
    public R<String> addOption(String activityId, VoteOptionReq req) {
        if (req.getId()!=null){
            //更新操作
            VoteOption option = baseMapper.selectById(req.getId());
            if (option == null) {
                return R.fail("选项不存在");
            }
            VoteOption option1 = BeanUtil.copyProperties(req, VoteOption.class);
            baseMapper.updateById(option1);
            return R.ok("更新成功");
        }
        //新增操作
        VoteOption option = baseMapper.selectOne(new QueryWrapper<VoteOption>().eq("activity_id", activityId).eq("option_name", req.getOptionName()));
        if (option != null) {
            return R.fail("选项已存在");
        }
        option = BeanUtil.copyProperties(req, VoteOption.class);
        option.setActivityId(activityId);
        baseMapper.insert(option);
        return R.ok("添加成功");
    }

    @Override
    public R<String> deleteOption(String optionId) {
        VoteOption option = baseMapper.selectById(optionId);
        if (option == null) {
            return R.fail("选项不存在");
        }
        baseMapper.deleteById(optionId);
        return R.ok("删除成功");
    }
}




