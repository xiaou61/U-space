package com.xiaou.bbs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.entity.BbsPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BbsPostMapper extends BaseMapper<BbsPost> {

    @Update("update `u_bbs_post` set comment_count = comment_count + #{i} where id = #{postId}")
    void updateCommentCountById(String postId, int i);

}




