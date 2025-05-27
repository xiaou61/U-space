package com.xiaou.bbs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
    @Update("UPDATE u_post_comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Update("UPDATE u_post_comment SET like_count = like_count - 1 WHERE id = #{commentId} AND like_count > 0")
    void decrementLikeCount(@Param("commentId") Long commentId);

}




