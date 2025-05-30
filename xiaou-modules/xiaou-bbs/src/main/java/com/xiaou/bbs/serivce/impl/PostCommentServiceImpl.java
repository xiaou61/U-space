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
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    //todo 暂时可能会有bug
    @Override
    public R<PageRespDto<PostCommentPageVo>> allPostCommentPage(PageReqDto dto) {
        // 分页查询一级评论（parentId = 0）
        IPage<PostComment> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", 0)
                .eq("is_deleted", 0);
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));
        IPage<PostComment> commentPage = baseMapper.selectPage(page, queryWrapper);

        List<PostComment> parentComments = commentPage.getRecords();
        if (parentComments.isEmpty()) {
            return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, Collections.emptyList()));
        }

        // 获取所有父评论 ID
        List<Long> parentIds = parentComments.stream()
                .map(PostComment::getId)
                .collect(Collectors.toList());

        // 查询子评论
        List<PostComment> childComments = baseMapper.selectList(
                new QueryWrapper<PostComment>()
                        .in("parent_id", parentIds)
                        .eq("is_deleted", 0)
                        .orderByAsc("create_time")
        );

        // 转换子评论为 VO 并按 parentId 分组
        Map<Long, List<PostCommentVo>> repliesMap = childComments.stream()
                .map(c -> {
                    PostCommentVo vo = new PostCommentVo();
                    BeanUtils.copyProperties(c, vo);
                    return vo;
                })
                .collect(Collectors.groupingBy(PostCommentVo::getParentId));

        // 转换父评论为 VO 并挂载 replies
        List<PostCommentPageVo> voList = parentComments.stream()
                .map(c -> {
                    PostCommentPageVo vo = new PostCommentPageVo();
                    BeanUtils.copyProperties(c, vo);
                    vo.setReplies(repliesMap.getOrDefault(c.getId(), Collections.emptyList()));
                    return vo;
                })
                .collect(Collectors.toList());

        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), commentPage.getTotal(), voList));
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


}




