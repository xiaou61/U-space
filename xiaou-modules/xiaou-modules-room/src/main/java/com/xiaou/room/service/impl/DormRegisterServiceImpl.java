package com.xiaou.room.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.room.domain.entity.DormBed;
import com.xiaou.room.domain.entity.DormRegister;
import com.xiaou.room.domain.req.DormRegisterReq;
import com.xiaou.room.mapper.DormBedMapper;
import com.xiaou.room.mapper.DormRegisterMapper;
import com.xiaou.room.service.DormRegisterService;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class DormRegisterServiceImpl extends ServiceImpl<DormRegisterMapper, DormRegister>
    implements DormRegisterService {
    @Resource
    private LoginHelper loginHelper;

    private final String grabbedKey = "dorm:bed:grabbed:";
    @Resource
    private DormBedMapper dormBedMapper;

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

    @Override
    public R<String> grab(String roomId, String bedId) {
        String userId = loginHelper.getCurrentAppUserId();
        String key = "dorm:grabbed:" + userId;

        if (RedisUtils.hasKey(key)) {
            return R.fail("你已经抢过床位");
        }

        String lockKey = "dorm:bed:lock:" + bedId;
        RLock lock = RedisUtils.lock(lockKey);

        try {
            if (!lock.tryLock(300, 5000, TimeUnit.MILLISECONDS)) {
                return R.fail("系统繁忙，请稍后再试");
            }
            DormBed bed = dormBedMapper.selectById(bedId);
            if (bed == null || Objects.equals(bed.getStatus(), GlobalConstants.ONE)) {
                return R.fail("该床位已被占用");
            }

            bed.setStatus(GlobalConstants.ONE);

            int updated = dormBedMapper.updateById(bed);
            if (updated == 0) {
                return R.fail("抢宿舍失败，请重试");
            }

            RedisUtils.setCacheObject(key, userId, Duration.ofHours(1));
            // todo mq
            return R.ok("抢宿舍成功！");
        } catch (Exception e) {
            log.error("抢宿舍异常", e);
            return R.fail("系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}




