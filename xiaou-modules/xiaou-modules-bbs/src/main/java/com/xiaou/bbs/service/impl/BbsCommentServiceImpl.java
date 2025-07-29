package com.xiaou.bbs.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.domain.entity.StudentEntity;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.bbs.domain.dto.CommentReplyCount;
import com.xiaou.bbs.domain.entity.BbsComment;
import com.xiaou.bbs.domain.entity.BbsCommentLike;
import com.xiaou.bbs.domain.req.BbsCommentReq;
import com.xiaou.bbs.domain.resp.BbsCommentResp;
import com.xiaou.bbs.mapper.BbsCommentLikeMapper;
import com.xiaou.bbs.mapper.BbsCommentMapper;
import com.xiaou.bbs.mapper.BbsCommentReplyMapper;
import com.xiaou.bbs.mapper.BbsPostMapper;
import com.xiaou.bbs.service.BbsCommentService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.service.SensitiveFilterResult;
import com.xiaou.service.SensitiveWordManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BbsCommentServiceImpl extends ServiceImpl<BbsCommentMapper, BbsComment>
    implements BbsCommentService {
    @Resource
    private LoginHelper loginHelper;
    @Resource
    private StudentMapper userMapper;
    @Resource
    private BbsCommentReplyMapper replyMapper;
    @Resource
    private BbsPostMapper bbsPostMapper;
    @Resource
    private BbsCommentLikeMapper commentLikeMapper;
    @Resource
    private BbsCommentMapper commentMapper;
    @Resource
    private SensitiveWordManager sensitiveWordManager;

    @Override
    @Transactional

    public R<String> addComment(BbsCommentReq req) {
        //添加一个评论
        //todo 发送消息通知
        BbsComment convert = MapstructUtils.convert(req, BbsComment.class);
        convert.setUserId(loginHelper.getCurrentAppUserId());
        //判断是否有敏感词
        SensitiveFilterResult filter = sensitiveWordManager.filter(req.getContent());
        if (!filter.isAllowed()) {
            return R.warn("包含敏感词不能发送");
        }
        //是否需要替换
        if (filter.getReplacedContent()!=null){
            log.info("替换内容：{}", filter.getReplacedContent());
            convert.setContent(filter.getReplacedContent());
        }
        baseMapper.insert(convert);
        //更新帖子的评论数量
        bbsPostMapper.updateCommentCountById(convert.getPostId(), 1);
        return R.ok("评论成功");
    }

    @Override
    @Transactional
    public R<String> deleteComment(String id) {
        //判断是否是自己的
        if (loginHelper.getCurrentAppUserId().equals(baseMapper.selectById(id).getUserId())) {
            //删除帖子的评论数量
            bbsPostMapper.updateCommentCountById(baseMapper.selectById(id).getPostId(), -1);
            baseMapper.deleteById(id);
            return R.ok("删除成功");
        }
        return R.fail("这个不是你的无法删除");
    }

    @Override
    public R<PageRespDto<BbsCommentResp>> getCommentList(String postId, PageReqDto dto) {
        // 构造分页参数
        IPage<BbsComment> iPage = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 按帖子ID查询一级评论，倒序
        QueryWrapper<BbsComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .orderByDesc("created_at");

        // 查询分页评论列表
        IPage<BbsComment> commentPage = baseMapper.selectPage(iPage, queryWrapper);
        List<BbsComment> commentList = commentPage.getRecords();

        // 提取所有用户ID，用于批量查询用户信息
        Set<String> userIds = commentList.stream()
                .map(BbsComment::getUserId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<String, StudentEntity> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(StudentEntity::getId, s -> s));

        // 批量查询每条评论的回复数
        List<String> commentIds = commentList.stream()
                .map(BbsComment::getId)
                .collect(Collectors.toList());

        Map<String, Long> replyCountMap = new HashMap<>();
        if (!commentIds.isEmpty()) {
            List<CommentReplyCount> counts = replyMapper.selectReplyCountByCommentIds(commentIds);
            for (CommentReplyCount item : counts) {
                replyCountMap.put(item.getCommentId(), item.getReplyCount());
            }

    }

        // 当前登录用户ID
        String currentUserId = loginHelper.getCurrentAppUserId();

        // 构造返回 DTO
        List<BbsCommentResp> respList = commentList.stream().map(comment -> {
            BbsCommentResp resp = MapstructUtils.convert(comment, BbsCommentResp.class);

            StudentEntity studentEntity = userMap.get(comment.getUserId());
            if (studentEntity != null) {
                resp.setName(studentEntity.getName());
                resp.setAvatar(studentEntity.getAvatar());
            }

            resp.setIsMine(currentUserId.equals(comment.getUserId()));
            resp.setReplyCount(replyCountMap.getOrDefault(comment.getId(), 0L));

            return resp;
        }).collect(Collectors.toList());

        return R.ok(PageRespDto.of(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), respList));
    }

    @Override
    public R<String> likeComment(String id) {
        //帖子评论点赞判断是否已经点过赞
        QueryWrapper<BbsCommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", id)
                .eq("user_id", loginHelper.getCurrentAppUserId());
        if (commentLikeMapper.selectOne(queryWrapper) != null) {
            //已经点过赞 也就是执行取消点赞的功能
            commentLikeMapper.delete(queryWrapper);
            commentMapper.updateLikeCountById(id, -1);
            return R.ok("取消点赞成功");
        }
        //没有点过赞 添加点赞
        BbsCommentLike like = new BbsCommentLike();
        like.setCommentId(id);
        like.setUserId(loginHelper.getCurrentAppUserId());
        commentLikeMapper.insert(like);
        commentMapper.updateLikeCountById(id, 1);
        //todo 消息通知
        return R.ok("点赞成功");
    }


}




