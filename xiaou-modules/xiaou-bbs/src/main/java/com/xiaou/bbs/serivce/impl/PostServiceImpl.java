package com.xiaou.bbs.serivce.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.bbs.domain.bo.PostBo;
import com.xiaou.bbs.domain.entity.Post;
import com.xiaou.bbs.domain.entity.PostLike;
import com.xiaou.bbs.domain.enums.PostCategoryEnum;
import com.xiaou.bbs.domain.page.CategoryPageReqDto;
import com.xiaou.bbs.domain.vo.PostCommentPageVo;
import com.xiaou.bbs.domain.vo.PostVo;
import com.xiaou.bbs.mapper.PostLikeMapper;
import com.xiaou.bbs.mapper.PostMapper;
import com.xiaou.bbs.serivce.PostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.notify.enums.NotificationTypeEnum;
import com.xiaou.notify.utils.NotificationUtils;
import com.xiaou.utils.LoginHelper;
import com.xiaou.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RSet;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {


    @Resource
    private PostLikeMapper postLikeMapper;

    @Resource
    private NotificationUtils notificationUtils;

    @Override
    public R<String> create(PostBo postBo) {
        Long userId = LoginHelper.getCurrentAppUserId();
        Post post = new Post();
        BeanUtils.copyProperties(postBo, post);
        post.setUserId(userId);
        post.setCategory(PostCategoryEnum.valueOf(postBo.getCategory()));
        post.setImageUrls(JSON.toJSONString(postBo.getImageUrls()));
        baseMapper.insert(post);
        return R.ok("创建成功");
    }

    @Override
    public R<PostVo> get(Long id) {
        Post post = baseMapper.selectById(id);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        PostVo postVo = new PostVo();
        BeanUtils.copyProperties(post, postVo);
        postVo.setImageUrls(JSON.parseArray(post.getImageUrls(), String.class));
        postVo.setCategory(String.valueOf(post.getCategory()));
        return R.ok(postVo);
    }



    @Override
    public R<String> delete(Long id) {
        //查询是不是自己的帖子
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
    public R<PageRespDto<PostVo>> allPostPage(PageReqDto dto) {
        // 创建分页对象，设置当前页码和每页大小
        IPage<Post> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());

        // 构造查询条件，并应用排序（例如按 create_time）
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));

        // 执行分页查询
        IPage<Post> postIPage = baseMapper.selectPage(page, queryWrapper);

        // 将实体列表转换为 VO 列表
        List<PostVo> voList = new ArrayList<>();
        for (Post post : postIPage.getRecords()) {
            PostVo postVo = new PostVo();
            // 复制基本属性
            BeanUtils.copyProperties(post, postVo);
            // 解析 imageUrls 字段（假设 post.getImageUrls() 返回的是 JSON 数组字符串）
            postVo.setImageUrls(JSON.parseArray(post.getImageUrls(), String.class));
            // 将 category 转为字符串
            postVo.setCategory(String.valueOf(post.getCategory()));
            voList.add(postVo);
        }

        // 封装分页响应
        PageRespDto<PostVo> pageResp = PageRespDto.of(
                dto.getPageNum(),
                dto.getPageSize(),
                postIPage.getTotal(),
                voList
        );

        return R.ok(pageResp);
    }


    @Override
    @Transactional
    public R<String> toggleLike(Long postId) {
        Long userId = LoginHelper.getCurrentAppUserId();
        String likeSetKey = "post:like:set:" + postId;
        String likeCountKey = "post:like:count:" + postId;

        // Redis中存的点赞集合，存userId
        RSet<Long> likeUserSet = RedisUtils.getClient().getSet(likeSetKey);
        RAtomicLong likeCount = RedisUtils.getClient().getAtomicLong(likeCountKey);

        boolean alreadyLiked = likeUserSet.contains(userId);

        if (alreadyLiked) {
            // 取消点赞
            likeUserSet.remove(userId);
            likeCount.decrementAndGet();

            // 同步数据库：删除点赞记录，点赞数-1
            postLikeMapper.delete(new QueryWrapper<PostLike>().eq("user_id", userId).eq("post_id", postId));
            baseMapper.decrementLikeCount(postId);

            return R.ok("取消点赞成功");
        } else {
            // 点赞
            likeUserSet.add(userId);
            likeCount.incrementAndGet();

            // 同步数据库：新增点赞记录，点赞数+1
            PostLike postLike = new PostLike();
            postLike.setUserId(userId);
            postLike.setPostId(postId);
            postLikeMapper.insert(postLike);
            baseMapper.incrementLikeCount(postId);

            // 点赞通知
            Post post = baseMapper.selectById(postId);
            if (post != null) {
                notificationUtils.sendNotification(
                        userId,
                        post.getUserId(),
                        NotificationTypeEnum.LIKE,
                        "你的帖子《" + post.getTitle() + "》被点赞了"
                );
            }

            return R.ok("点赞成功");
        }
    }


    @Override
    public void addViewCount(Long postId) {
        baseMapper.incrementViewCount(postId);
    }

    @Override
    public List<PostVo> searchPosts(String keyword) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .like(Post::getTitle, keyword)
                .or()
                .like(Post::getContent, keyword);

        List<Post> posts = this.list(wrapper);

        List<PostVo> voList = new ArrayList<>();
        for (Post post : posts) {
            PostVo postVo = new PostVo();
            BeanUtils.copyProperties(post, postVo);
            postVo.setImageUrls(JSON.parseArray(post.getImageUrls(), String.class));
            postVo.setCategory(String.valueOf(post.getCategory()));
            voList.add(postVo);
        }

        return voList;
    }


    @Override
    public R<PageRespDto<PostVo>> pageByCategory(CategoryPageReqDto dto) {
        // 创建分页对象
        IPage<Post> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());

        // 构建查询条件
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(dto.getCategory())) {
            queryWrapper.eq("category", dto.getCategory());
        }

        // 排序处理
        queryWrapper.orderBy(
                StrUtil.isNotBlank(dto.getSortField()),
                dto.getDesc(),
                dto.getSortField()
        );

        // 分页查询
        IPage<Post> postIPage = baseMapper.selectPage(page, queryWrapper);

        // 转换为 PostVo 列表
        List<PostVo> voList = new ArrayList<>();
        for (Post post : postIPage.getRecords()) {
            PostVo postVo = new PostVo();
            BeanUtils.copyProperties(post, postVo);
            postVo.setImageUrls(JSON.parseArray(post.getImageUrls(), String.class));
            postVo.setCategory(String.valueOf(post.getCategory()));
            voList.add(postVo);
        }

        // 构建分页响应对象
        PageRespDto<PostVo> pageResp = PageRespDto.of(
                dto.getPageNum(),
                dto.getPageSize(),
                postIPage.getTotal(),
                voList
        );

        return R.ok(pageResp);
    }


    public Long countNewPostsSince(LocalDateTime lastRefreshTime) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (lastRefreshTime != null) {
            queryWrapper.gt("update_time", lastRefreshTime);
        }
        queryWrapper.eq("status", 1);      // 只统计正常帖
        queryWrapper.eq("is_deleted", 0);  // 过滤删除帖
        return baseMapper.selectCount(queryWrapper);
    }


}




