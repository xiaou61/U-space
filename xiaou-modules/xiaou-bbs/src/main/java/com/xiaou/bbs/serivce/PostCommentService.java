package com.xiaou.bbs.serivce;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.bo.PostCommentBo;
import com.xiaou.bbs.domain.entity.PostComment;
import com.xiaou.bbs.domain.vo.PostCommentPageVo;
import com.xiaou.bbs.domain.vo.PostCommentVo;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;


public interface PostCommentService extends IService<PostComment> {

    R<String> create(PostCommentBo bo);

    R<String> delete(Long id);

    R<PageRespDto<PostCommentPageVo>> allPostCommentPage(PageReqDto dto);


    R<String> toggleCommentLike(Long commentId);
}
