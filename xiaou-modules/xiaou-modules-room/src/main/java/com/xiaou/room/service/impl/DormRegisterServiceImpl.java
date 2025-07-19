package com.xiaou.room.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.room.domain.entity.DormRegister;
import com.xiaou.room.domain.req.DormRegisterReq;
import com.xiaou.room.mapper.DormRegisterMapper;
import com.xiaou.room.service.DormRegisterService;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DormRegisterServiceImpl extends ServiceImpl<DormRegisterMapper, DormRegister>
    implements DormRegisterService {
    @Resource
    private LoginHelper loginHelper;

    @Override
    public R<Boolean> isNeedInputInfo() {
        //todo 判断缓存是否有
        if (RedisUtils.hasKey("dorm_register:"+loginHelper.getCurrentAppUserId())) {
            return R.ok(false);
        }
        //否则进行数据库查询
        String currentAppUserId = loginHelper.getCurrentAppUserId();
        QueryWrapper<DormRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",currentAppUserId);
        if (baseMapper.selectOne(queryWrapper) == null) {
            return R.ok(true);
        }
        return R.ok(false);
    }

    @Override
    public R<String> inputInfo(DormRegisterReq req) {
        DormRegister convert = MapstructUtils.convert(req, DormRegister.class);
        convert.setUserId(loginHelper.getCurrentAppUserId());
        baseMapper.insert(convert);
        //todo redis进行缓存
        RedisUtils.setCacheObject("dorm_register:"+loginHelper.getCurrentAppUserId(),convert);
        return R.ok("填充成功");
    }
}




