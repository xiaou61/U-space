package com.xiaou.room.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.room.domain.entity.DormRegister;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DormRegisterMapper extends BaseMapper<DormRegister> {

    @Select("select * from u_dorm_register where user_id = #{currentAppUserId}")
    DormRegister selectByUserid(String currentAppUserId);
}




