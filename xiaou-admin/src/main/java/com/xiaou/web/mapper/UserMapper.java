package com.xiaou.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.web.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper  extends BaseMapper<User> {
}
