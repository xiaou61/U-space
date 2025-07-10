package com.xiaou.bbs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.entity.BbsComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BbsCommentMapper extends BaseMapper<BbsComment> {


    @Update("update `u_bbs_comment` set like_count = like_count + #{i} where id = #{id}")
    void updateLikeCountById(String id, int i);
}




