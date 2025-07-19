package com.xiaou.room.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.room.domain.entity.DormBed;
import com.xiaou.room.domain.entity.DormRegister;
import com.xiaou.room.domain.entity.DormRoom;
import com.xiaou.room.domain.req.DormRoomReq;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormRoomResp;
import com.xiaou.room.domain.resp.ListMyResp;
import com.xiaou.room.mapper.DormBedMapper;
import com.xiaou.room.mapper.DormBuildingMapper;
import com.xiaou.room.mapper.DormRegisterMapper;
import com.xiaou.room.mapper.DormRoomMapper;
import com.xiaou.room.service.DormRoomService;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom>
    implements DormRoomService {

    @Resource
    private  LoginHelper loginHelper;
    @Resource
    private DormRegisterMapper dormRegisterMapper;

    @Resource
    private DormBuildingMapper dormBuildingMapper;

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

    @Override
    public R<List<ListMyResp>> listMy() {
        // 当前用户 ID
        String currentAppUserId = loginHelper.getCurrentAppUserId();

        // 获取用户登记信息
        DormRegister dormRegister = dormRegisterMapper.selectByUserid(currentAppUserId);
        //需要判断是否为空
        if (dormRegister == null) {
            return R.fail("请先登记信息");
        }
        Integer gender = dormRegister.getGender();

        // 查询对应性别的宿舍房间
        List<DormRoom> dormRooms = baseMapper.selectList(new QueryWrapper<DormRoom>().eq("gender", gender));
        if (CollUtil.isEmpty(dormRooms)) {
            return R.ok(Collections.emptyList());
        }

        // 转换为响应对象
        List<ListMyResp> respList = MapstructUtils.convert(dormRooms, ListMyResp.class);

        // 批量获取所有房间ID
        List<String> roomIds = dormRooms.stream()
                .map(DormRoom::getId)
                .collect(Collectors.toList());

        // 批量查询所有房间的床位信息
        List<DormBed> dormBeds = baseMapper.listBedByRoomIds(roomIds);
        Map<String, List<DormBed>> roomIdToBeds = dormBeds.stream()
                .collect(Collectors.groupingBy(DormBed::getRoomId));

        // 组装床位信息
        for (ListMyResp resp : respList) {
            List<DormBed> beds = roomIdToBeds.getOrDefault(resp.getId(), Collections.emptyList());
            resp.setDormBeds(MapstructUtils.convert(beds, DormBedResp.class));
            //根据buildingId 查询buildingName
            resp.setBuildingName(dormBuildingMapper.selectNameById(resp.getBuildingId()));
        }

        return R.ok(respList);

}
}




