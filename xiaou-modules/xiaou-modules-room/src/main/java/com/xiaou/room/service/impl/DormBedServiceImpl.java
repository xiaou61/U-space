package com.xiaou.room.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.room.domain.entity.DormBed;
import com.xiaou.room.domain.req.DormBedReq;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormBuildingResp;
import com.xiaou.room.domain.resp.DormRoomResp;
import com.xiaou.room.mapper.DormBedMapper;
import com.xiaou.room.service.DormBedService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed>
    implements DormBedService {

    @Override
    public R<String> add(DormBedReq req) {
        DormBed dormBed = MapstructUtils.convert(req, DormBed.class);
        baseMapper.insert(dormBed);
        return R.ok("添加成功");
    }

    @Override
    public R<String> removeBed(String id) {
        //如果改床被占用，则不能删除
        DormBed dormBed = baseMapper.selectById(id);
        if (dormBed.getStatus() == 1) {
            return R.fail("该床被占用，请先释放");
        }
        return baseMapper.deleteById(id) > 0 ? R.ok("删除成功") : R.fail("删除失败");
    }

    @Override
    public R<List<DormBedResp>> listBed(String roomId) {
        List<DormBed> dormBeds = baseMapper.listBed(roomId);
        List<DormBedResp> convert = MapstructUtils.convert(dormBeds, DormBedResp.class);
        return R.ok(convert);
    }

    @Override
    public List<DormBedResp> listDormBedByRoomIds(List<String> collect) {
        List<DormBed> dormBeds = baseMapper.selectBedByRoomIds(collect);
        List<DormBedResp> convert = MapstructUtils.convert(dormBeds, DormBedResp.class);
        return convert;
    }
}




