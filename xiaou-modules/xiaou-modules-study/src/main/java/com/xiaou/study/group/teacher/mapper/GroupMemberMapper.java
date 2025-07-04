package com.xiaou.study.group.teacher.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.study.group.teacher.domain.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

}




