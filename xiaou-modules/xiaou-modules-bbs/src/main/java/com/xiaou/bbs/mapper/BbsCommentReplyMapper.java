package com.xiaou.bbs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.bbs.domain.dto.CommentReplyCount;
import com.xiaou.bbs.domain.entity.BbsCommentReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BbsCommentReplyMapper extends BaseMapper<BbsCommentReply> {

    /**
     * 批量统计每个评论的回复数
     * @param commentIds 评论ID列表
     * @return 每个评论对应的回复数
     */
    List<CommentReplyCount> selectReplyCountByCommentIds(@Param("commentIds") List<String> commentIds);

    @Update("update `u_bbs_comment_reply` set like_count = like_count + #{i} where id = #{id}")
    void updateLikeCountById(String id, int i);
}




