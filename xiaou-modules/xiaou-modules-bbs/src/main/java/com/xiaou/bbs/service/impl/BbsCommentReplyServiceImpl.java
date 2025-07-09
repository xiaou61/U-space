package com.xiaou.bbs.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.bbs.domain.entity.BbsCommentReply;
import com.xiaou.bbs.domain.req.BbsCommentReplyReq;
import com.xiaou.bbs.domain.resp.BbsCommentReplyResp;
import com.xiaou.bbs.mapper.BbsCommentReplyMapper;
import com.xiaou.bbs.service.BbsCommentReplyService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BbsCommentReplyServiceImpl extends ServiceImpl<BbsCommentReplyMapper, BbsCommentReply>
    implements BbsCommentReplyService {

    @Resource
    private LoginHelper loginHelper;
    @Resource
    private StudentMapper userMapper;
    @Override
    public R<String> replyComment(BbsCommentReplyReq req) {
        //todo 消息通知
        BbsCommentReply convert = MapstructUtils.convert(req, BbsCommentReply.class);
        convert.setUserId(loginHelper.getCurrentAppUserId());
        baseMapper.insert(convert);
        return R.ok("回复成功");
    }

    @Override
    public R<String> deleteReply(String id) {
        //判断是否是自己的回复
        if (loginHelper.getCurrentAppUserId().equals(baseMapper.selectById(id).getUserId())) {
            baseMapper.deleteById(id);
            return R.ok("删除回复成功");
        }
        return R.fail("这个回复不是你的无法删除");
    }

    @Override
    public R<PageRespDto<BbsCommentReplyResp>> getReplyList(String commentId, PageReqDto dto) {
        // 分页构造器
        IPage<BbsCommentReply> iPage = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 查询条件：回复所属的评论ID，按时间倒序
        QueryWrapper<BbsCommentReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .orderByDesc("created_at");

        // 执行分页查询
        IPage<BbsCommentReply> replyPage = baseMapper.selectPage(iPage, queryWrapper);
        List<BbsCommentReply> replyList = replyPage.getRecords();

        // 收集所有涉及用户ID（回复人 + 被回复人）
        Set<String> userIds = new HashSet<>();
        for (BbsCommentReply reply : replyList) {
            userIds.add(reply.getUserId());
            if (reply.getReplyUserId() != null) {
                userIds.add(reply.getReplyUserId());
            }
        }

        // 批量查询所有用户信息
        Map<String, Student> userMap;
        if (!userIds.isEmpty()) {
            List<Student> students = userMapper.selectBatchIds(userIds);
            userMap = students.stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
        } else {
            userMap = new HashMap<>();
        }

        // 当前登录用户ID
        String currentUserId = loginHelper.getCurrentAppUserId();

        // 转换成响应DTO并填充用户信息
        List<BbsCommentReplyResp> respList = replyList.stream().map(reply -> {
            BbsCommentReplyResp resp = MapstructUtils.convert(reply, BbsCommentReplyResp.class);

            // 是否是自己发布的回复
            resp.setIsMine(currentUserId.equals(reply.getUserId()));

            // 回复人的信息
            Student replyUser = userMap.get(reply.getUserId());
            if (replyUser != null) {
                resp.setReplyUserName(replyUser.getName());
                resp.setReplyUserAvatar(replyUser.getAvatar());
            }

            // 被回复人的信息
            Student replyToUser = userMap.get(reply.getReplyUserId());
            if (replyToUser != null) {
                resp.setReplyToUserName(replyToUser.getName());
                resp.setReplyToUserAvatar(replyToUser.getAvatar());
            }

            return resp;
        }).collect(Collectors.toList());

        // 返回分页结果
        return R.ok(PageRespDto.of(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), respList));
    }

}




