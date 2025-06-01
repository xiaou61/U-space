package com.xiaou.bbs.serivce.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.bbs.domain.bo.PostCommentBo;
import com.xiaou.bbs.domain.entity.Post;
import com.xiaou.bbs.domain.entity.PostComment;
import com.xiaou.bbs.domain.entity.PostCommentLike;
import com.xiaou.bbs.domain.vo.PostCommentPageVo;
import com.xiaou.bbs.domain.vo.PostCommentVo;
import com.xiaou.bbs.mapper.PostCommentLikeMapper;
import com.xiaou.bbs.mapper.PostCommentMapper;
import com.xiaou.bbs.mapper.PostMapper;
import com.xiaou.bbs.serivce.PostCommentService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.notify.enums.NotificationTypeEnum;
import com.xiaou.notify.utils.NotificationUtils;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.service.StudentUserService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment>
        implements PostCommentService {
    @Resource
    private PostCommentLikeMapper postCommentLikeMapper;

    @Resource
    private NotificationUtils notificationUtils;

    @Resource
    private PostMapper postMapper;  // 查询帖子

    @Resource
    private StudentUserService userService;

    @Override
    @Transactional
    public R<String> create(PostCommentBo bo) {
        Long userId = LoginHelper.getCurrentAppUserId();
        PostComment postComment = MapstructUtils.convert(bo, PostComment.class);
        postComment.setUserId(userId);

        boolean saved = save(postComment);
        if (!saved) {
            return R.fail("评论失败");
        }

        // 发送评论通知
        Post post = postMapper.selectById(postComment.getPostId());
        if (post != null) {
            notificationUtils.sendNotification(
                    userId,
                    post.getUserId(),
                    NotificationTypeEnum.COMMENT,
                    "你的帖子《" + post.getTitle() + "》有了新评论"
            );
        }

        return R.ok("评论成功");
    }


    @Override
    public R<String> delete(Long id) {
        //只能删除自己发的帖子评论
        Long userId = LoginHelper.getCurrentAppUserId();
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("id", id);
        return R.ok(remove(queryWrapper) ? "删除成功" : "删除失败");
    }



    @Override
    @Transactional
    public R<String> toggleCommentLike(Long commentId) {
        Long userId = LoginHelper.getCurrentAppUserId();

        // 1. 检查是否已点赞
        QueryWrapper<PostCommentLike> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("comment_id", commentId);
        PostCommentLike exist = postCommentLikeMapper.selectOne(qw);

        if (exist != null) {
            // 已点赞 → 取消
            postCommentLikeMapper.deleteById(exist.getId());
            baseMapper.decrementLikeCount(commentId);
            return R.ok("取消评论点赞成功");
        } else {
            // 未点赞 → 点赞
            PostCommentLike like = new PostCommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            postCommentLikeMapper.insert(like);
            baseMapper.incrementLikeCount(commentId);

            //通知逻辑
            PostComment comment = baseMapper.selectById(commentId);
            if (comment != null && !comment.getUserId().equals(userId)) {
                notificationUtils.sendNotification(
                        userId,
                        comment.getUserId(),
                        NotificationTypeEnum.LIKE,
                        "你的评论被点赞了"
                );
            }
            return R.ok("评论点赞成功");
        }
    }

    @Override
    public R<PageRespDto<PostCommentPageVo>> allPostCommentPage(PageReqDto dto) {
        // 1. 分页查询【所有帖子的】一级评论（parent_id = 0）
        IPage<PostComment> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("parent_id", 0)
                .eq("is_deleted", 0);
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));
        IPage<PostComment> commentPage = baseMapper.selectPage(page, queryWrapper);

        List<PostComment> parentComments = commentPage.getRecords();
        if (parentComments.isEmpty()) {
            return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, Collections.emptyList()));
        }

        // 接下来的逻辑和 pageById 完全一致：拿到 parentIds，查 childComments，批量查 userInfo，组装 VO
        return assembleCommentPageVo(dto, commentPage, parentComments);
    }

    @Override
    public R<PageRespDto<PostCommentPageVo>> pageById(PageReqDto dto, Long postId) {
        // 1. 分页查询【指定帖子的】一级评论（parent_id = 0 AND post_id = :postId）
        IPage<PostComment> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("parent_id", 0)
                .eq("is_deleted", 0)
                .eq("post_id", postId);           // ← 加上 postId 过滤
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));
        IPage<PostComment> commentPage = baseMapper.selectPage(page, queryWrapper);

        List<PostComment> parentComments = commentPage.getRecords();
        if (parentComments.isEmpty()) {
            return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, Collections.emptyList()));
        }

        // 继续通用组装逻辑
        return assembleCommentPageVo(dto, commentPage, parentComments);
    }

    /**
     * 公共的“把 parentComments 和 childComments + 用户信息 批量组装成 VO 列表”逻辑
     */
    private R<PageRespDto<PostCommentPageVo>> assembleCommentPageVo(
            PageReqDto dto,
            IPage<PostComment> commentPage,
            List<PostComment> parentComments
    ) {
        // 2. 拿到所有 parent 评论的 ID
        List<Long> parentIds = parentComments.stream()
                .map(PostComment::getId)
                .collect(Collectors.toList());

        // 3. 批量查询子评论（parent_id IN parentIds）
        List<PostComment> childComments = baseMapper.selectList(
                new QueryWrapper<PostComment>()
                        .in("parent_id", parentIds)
                        .eq("is_deleted", 0)
                        .orderByAsc("create_time")
        );

        // 4. 收集所有涉及的 userId（父 & 子），批量查 StudentUser → Map<userId, StudentUser>
        Set<Long> userIds = new HashSet<>();
        parentComments.forEach(c -> userIds.add(c.getUserId()));
        childComments.forEach(c -> userIds.add(c.getUserId()));

        List<StudentUser> users = userService.listByIds(userIds);
        Map<Long, StudentUser> userMap = users.stream()
                .collect(Collectors.toMap(StudentUser::getId, Function.identity()));

        // 5. 把子评论转换为 PostCommentVo，填充昵称&头像，然后根据 parentId 分组
        Map<Long, List<PostCommentVo>> repliesMap = childComments.stream()
                .map(child -> {
                    PostCommentVo vo = new PostCommentVo();
                    BeanUtils.copyProperties(child, vo);
                    StudentUser u = userMap.get(child.getUserId());
                    if (u != null) {
                        vo.setNickname(u.getName());
                        vo.setAvatarUrl(u.getAvatarUrl());
                    }
                    return vo;
                })
                .collect(Collectors.groupingBy(PostCommentVo::getParentId));

        // 6. 把父评论转换为 PostCommentPageVo，填充昵称&头像，并挂载对应的 replies 列表
        List<PostCommentPageVo> voList = parentComments.stream()
                .map(parent -> {
                    PostCommentPageVo vo = new PostCommentPageVo();
                    BeanUtils.copyProperties(parent, vo);
                    StudentUser u = userMap.get(parent.getUserId());
                    if (u != null) {
                        vo.setNickname(u.getName());
                        vo.setAvatarUrl(u.getAvatarUrl());
                    }
                    // 挂载该父评论的子评论列表
                    vo.setReplies(repliesMap.getOrDefault(parent.getId(), Collections.emptyList()));
                    return vo;
                })
                .collect(Collectors.toList());

        // 7. 返回分页结果
        PageRespDto<PostCommentPageVo> pageResp = PageRespDto.of(
                dto.getPageNum(),
                dto.getPageSize(),
                commentPage.getTotal(),
                voList
        );
        return R.ok(pageResp);
    }

}




