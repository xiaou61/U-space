package com.xiaou.room.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.room.domain.entity.DormBuilding;
import com.xiaou.room.domain.req.DormBuildingReq;
import com.xiaou.room.domain.resp.DormAllResp;
import com.xiaou.room.domain.resp.DormBedResp;
import com.xiaou.room.domain.resp.DormBuildingResp;
import com.xiaou.room.domain.resp.DormRoomResp;
import com.xiaou.room.mapper.DormBuildingMapper;
import com.xiaou.room.service.DormBedService;
import com.xiaou.room.service.DormBuildingService;
import com.xiaou.room.service.DormRoomService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DormBuildingServiceImpl extends ServiceImpl<DormBuildingMapper, DormBuilding>
    implements DormBuildingService {

    @Resource
    private DormRoomService dormRoomService;
    @Resource
    private DormBedService dormBedService;
    @Override
    public R<String> add(DormBuildingReq req) {
        DormBuilding building = MapstructUtils.convert(req, DormBuilding.class);
        baseMapper.insert(building);
        return R.ok("添加成功");
    }

    @Override
    public R<String> removeBuilding(String id) {
        //如果下面有宿舍，则不能删除
        if (baseMapper.hasDormRoom(id)) {
            return R.fail("该宿舍楼下有宿舍，请先删除宿舍");
        }
        return baseMapper.deleteById(id) > 0 ? R.ok("删除成功") : R.fail("删除失败");
    }

    @Override
    public R<List<DormBuildingResp>> listBuilding() {
        List<DormBuilding> list = baseMapper.selectList(null);
        List<DormBuildingResp> convert = MapstructUtils.convert(list, DormBuildingResp.class);
        return R.ok(convert);
    }

    @Override
    public R<PageRespDto<DormAllResp>> listAll(PageReqDto req) {
        IPage<DormBuilding> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<DormBuilding> dormBuildingIPage = baseMapper.selectPage(page, null);
        //建筑信息
        List<DormBuildingResp> dormBuildingResps = MapstructUtils.convert(dormBuildingIPage.getRecords(), DormBuildingResp.class);
        //根据建筑id查询房间信息
        List<DormRoomResp> dormRoomResps = dormRoomService.listDormRoomByBuildingIds(dormBuildingResps.stream().map(DormBuildingResp::getId).collect(Collectors.toList()));
        //根据房间id查询床位信息
        List<DormBedResp> dormBedResps = dormBedService.listDormBedByRoomIds(dormRoomResps.stream().map(DormRoomResp::getId).collect(Collectors.toList()));
        //统一组装一下
        DormAllResp dormAllResp = new DormAllResp();
        dormAllResp.setDormBuildings(dormBuildingResps);
        dormAllResp.setDormRooms(dormRoomResps);
        dormAllResp.setDormBeds(dormBedResps);

        return R.ok(
                PageRespDto.of(req.getPageNum(),req.getPageSize(),
                        page.getTotal(),
                        List.of(dormAllResp))
        );
    }
}




