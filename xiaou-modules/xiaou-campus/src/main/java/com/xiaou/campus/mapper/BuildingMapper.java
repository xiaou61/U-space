package com.xiaou.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.campus.domain.entity.BuildingInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BuildingMapper extends BaseMapper<BuildingInfo> {
}
