package com.xiaou.room.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.room.domain.entity.DormBed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DormBedMapper extends BaseMapper<DormBed> {


    @Select("select * from u_dorm_bed where room_id= #{roomId}")
    List<DormBed> listBed(String roomId);


    List<DormBed> selectBedByRoomIds(@Param("roomIds") List<String> roomIds);
}




