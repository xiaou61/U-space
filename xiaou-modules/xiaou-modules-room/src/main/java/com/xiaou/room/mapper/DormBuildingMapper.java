package com.xiaou.room.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.room.domain.entity.DormBuilding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DormBuildingMapper extends BaseMapper<DormBuilding> {

    @Select("select count(1) from u_drom_room where building_id = #{id}")
    boolean hasDormRoom(String id);
}




