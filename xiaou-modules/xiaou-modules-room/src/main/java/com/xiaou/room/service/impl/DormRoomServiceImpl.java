package com.xiaou.room.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.room.domain.entity.DormRoom;
import com.xiaou.room.domain.req.DormRoomReq;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormBuildingResp;
import com.xiaou.room.domain.resp.DormRoomResp;
import com.xiaou.room.mapper.DormRoomMapper;
import com.xiaou.room.service.DormRoomService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom>
    implements DormRoomService {

    @Override
    public R<String> add(DormRoomReq req) {
        DormRoom room = MapstructUtils.convert(req, DormRoom.class);
        room.setAvailable(room.getCapacity());
        baseMapper.insert(room);
        //todo 当前剩余可选床位数需要缓存到redis里面去

        return R.ok("添加成功");
    }

    @Override
    public R<String> removeRoom(String id) {
        //如果宿舍下有宿舍床，则不能删除
        if (baseMapper.hasDormBed(id)) {
            return R.fail("该宿舍楼下有宿舍床，请先删除宿舍床");
        }
        return baseMapper.deleteById(id) > 0 ? R.ok("删除成功") : R.fail("删除失败");

    }

    @Override
    public R<List<DormRoomResp>> listRoom(String buildingId) {
        List<DormRoom> list = baseMapper.listRoom(buildingId);
        List<DormRoomResp> convert = MapstructUtils.convert(list, DormRoomResp.class);
        return R.ok(convert);
    }

    @Override
    public List<DormRoomResp> listDormRoomByBuildingIds(List<String> collect) {

        List<DormRoom> list = baseMapper.seletRoomByIds(collect);
        List<DormRoomResp> convert = MapstructUtils.convert(list, DormRoomResp.class);
        return convert;
    }
}




