package com.xiaou.bbs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.entity.BbsUserNotify;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BbsUserNotifyMapper extends BaseMapper<BbsUserNotify> {

    @Update("update u_bbs_user_notify set is_read = 1 where receiver_id = #{currentAppUserId} and is_read=0")
    void updateIsRead(String currentAppUserId);
}




