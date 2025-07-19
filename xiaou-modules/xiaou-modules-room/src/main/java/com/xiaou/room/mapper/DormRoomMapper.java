package com.xiaou.room.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.room.domain.entity.DormBed;
import com.xiaou.room.domain.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {

    @Select("select count(1) from u_dorm_bed where room_id=#{id}")
    boolean hasDormBed(String id);

    @Select("select * from u_dorm_room where building_id=#{buildingId}")
    List<DormRoom> listRoom(String buildingId);


    List<DormRoom> seletRoomByIds(@Param("ids") List<String> ids);


    @Select("select * from u_dorm_room where id=#{id}")
    List<Object> listBed(String id);

    List<DormBed> listBedByRoomIds(List<String> roomIds);
}




