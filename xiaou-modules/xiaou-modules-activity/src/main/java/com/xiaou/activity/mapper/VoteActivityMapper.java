package com.xiaou.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.activity.domain.entity.VoteActivity;
import com.xiaou.activity.domain.resp.VoteActivityResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VoteActivityMapper extends BaseMapper<VoteActivity> {

    /**
     * 分页查询投票活动及其选项信息
     * @param page 分页对象
     * @param orderByClause 排序条件
     * @return 投票活动响应列表
     */
    IPage<VoteActivityResp> selectVoteActivitiesWithOptions(
            IPage<VoteActivityResp> page, 
            @Param("orderByClause") String orderByClause
    );
}




