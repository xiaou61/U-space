package com.xiaou.bbs.serivce;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.entity.Post;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.common.domain.R;


public interface PostService extends IService<Post> {

    R<String> create(PostBo postBo);

    R<PostVo> get(Long id);

    R<String> delete(Long id);

    R<String> edit(Long id, PostBo postBo);
}
