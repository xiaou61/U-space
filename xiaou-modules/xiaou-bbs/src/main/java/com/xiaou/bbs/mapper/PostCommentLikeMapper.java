package com.xiaou.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.entity.PostCommentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostCommentLikeMapper extends BaseMapper<PostCommentLike> {
    @Delete("DELETE FROM u_post_comment_like WHERE user_id = #{userId} AND comment_id = #{commentId}")
    int deleteByUserAndComment(@Param("userId") Long userId, @Param("commentId") Long commentId);
}
