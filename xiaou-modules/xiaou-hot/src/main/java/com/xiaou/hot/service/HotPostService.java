package com.xiaou.hot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostVO;

import java.util.List;

public interface HotPostService extends IService<HotPost> {

    /**
     * 获取热门帖子列表
     *
     * @return {@link List }<{@link HotPostVO }>
     */
    List<HotPostVO> getHotPostList();
}