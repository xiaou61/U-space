package com.xiaou.bbs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.entity.BbsPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BbsPostMapper extends BaseMapper<BbsPost> {

    @Update("update `u_bbs_post` set comment_count = comment_count + #{i} where id = #{postId}")
    void updateCommentCountById(String postId, int i);

    @Update("update `u_bbs_post` set view_count = view_count + #{i} where id = #{id}")
    void updateViewCountById(String id, int i);


    @Select("select * from `u_bbs_post` where created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)")
    List<BbsPost> listPostsInLastSevenDays();

    @Select("select * from `u_bbs_post` where id = #{id}")
    BbsPost getById(String id);
}




