package com.xiaou.bbs.serivce;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.entity.Post;
import com.xiaou.bbs.domain.page.CategoryPageReqDto;
import com.xiaou.bbs.domain.vo.PostLikeInfoVo;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

import java.time.LocalDateTime;
import java.util.List;


public interface PostService extends IService<Post> {

    R<String> create(PostBo postBo);

    R<PostVo> get(Long id);

    R<String> delete(Long id);



    R<PageRespDto<PostVo>> allPostPage(PageReqDto dto);

    /**
     * 点赞/取消点赞切换
     */
    R<String> toggleLike(Long postId);

    void addViewCount(Long postId);

    List<PostVo> searchPosts(java.lang.String keyword);

    R<PageRespDto<PostVo>> pageByCategory(CategoryPageReqDto dto);

    Long countNewPostsSince(LocalDateTime lastRefreshTime);

    R<List<PostLikeInfoVo>> getCurrentUserLikedPosts();

    Long countLiked();
}
