package com.xiaou.bbs.serivce.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.entity.Post;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.bbs.mapper.PostMapper;
import com.xiaou.bbs.serivce.PostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.utils.LoginHelper;
import org.springframework.stereotype.Service;


@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    @Override
    public R<String> create(PostBo postBo) {
        Long userId = LoginHelper.getCurrentAppUserId();
        Post post = MapstructUtils.convert(postBo, Post.class);
        post.setUserId(userId);
        baseMapper.insert(post);
        return R.ok("创建成功");
    }

    @Override
    public R<PostVo> get(Long id) {
        Post post = baseMapper.selectById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        return R.ok(MapstructUtils.convert(post, PostVo.class));
    }

    @Override
    public R<String> delete(Long id) {
        //检测是否删除的是自己的帖子或者当前的管理员
        Long userId = LoginHelper.getCurrentAppUserId();

        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("id", id);
        Post post = baseMapper.selectOne(queryWrapper);
        if (post == null) {
            return R.fail("非法请求");
        }

        //删除帖子
        baseMapper.deleteById(id);

        return R.ok("删除成功");
    }

    @Override
    public R<String> edit(Long id, PostBo postBo) {
        //判断是否编辑的是自己的帖子
        Long userId = LoginHelper.getCurrentAppUserId();
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("id", id);
        Post post = baseMapper.selectOne(queryWrapper);
        if (post == null) {
            return R.fail("非法请求");
        }
        //编辑帖子
        post = MapstructUtils.convert(postBo, Post.class);
        baseMapper.updateById(post);
        return R.ok("编辑成功");
    }
}




