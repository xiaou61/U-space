package com.xiaou.bbs.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.bbs.domain.entity.BbsPost;
import com.xiaou.bbs.domain.entity.BbsUserNotify;
import com.xiaou.bbs.domain.resp.BbsUserNotifyResp;
import com.xiaou.bbs.mapper.BbsPostMapper;
import com.xiaou.bbs.mapper.BbsUserNotifyMapper;
import com.xiaou.bbs.service.BbsUserNotifyService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BbsUserNotifyServiceImpl extends ServiceImpl<BbsUserNotifyMapper, BbsUserNotify>
    implements BbsUserNotifyService {
    @Resource
    private BbsUserNotifyMapper bbsUserNotifyMapper;
    @Resource
    private BbsPostMapper postMapper;
    @Resource
    private StudentMapper studentMapper;

    @Override
    public R<PageRespDto<BbsUserNotifyResp>> pageList(PageReqDto dto) {
        IPage<BbsUserNotify> iPage = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 1. 查询通知分页数据，按时间倒序
        QueryWrapper<BbsUserNotify> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        IPage<BbsUserNotify> page = bbsUserNotifyMapper.selectPage(iPage, queryWrapper);

        List<BbsUserNotifyResp> respList = MapstructUtils.convert(page.getRecords(), BbsUserNotifyResp.class);

        if (respList.isEmpty()) {
            return R.ok(PageRespDto.of(page.getCurrent(), page.getSize(), page.getTotal(), respList));
        }


        // 2.1 批量提取 senderId，避免每条记录单独查
        Set<String> senderIds = respList.stream()
                .map(BbsUserNotifyResp::getSenderId)
                .collect(Collectors.toSet());

        Map<String, Student> senderMap = studentMapper.selectBatchIds(senderIds)
                .stream().collect(Collectors.toMap(Student::getId, s -> s));

        // 2.2 提取所有 type = post_like 的 targetId（即 postId）
        Set<String> postIds = respList.stream()
                .filter(resp -> "post_like".equals(resp.getType()))
                .map(BbsUserNotifyResp::getTargetId)
                .collect(Collectors.toSet());

        Map<String, BbsPost> postMap = postIds.isEmpty() ? Collections.emptyMap() :
                postMapper.selectBatchIds(postIds).stream()
                        .collect(Collectors.toMap(BbsPost::getId, p -> p));

        // 3. 数据填充：昵称、头像、帖子标题
        for (BbsUserNotifyResp resp : respList) {
            // 3.1 设置发送者昵称和头像
            Student sender = senderMap.get(resp.getSenderId());
            if (sender != null) {
                resp.setSenderName(sender.getName());
                resp.setSenderAvatar(sender.getAvatar());
            }

            // 3.2 填充帖子标题（目前只处理 post_like）
            if ("post_like".equals(resp.getType())) {
                BbsPost post = postMap.get(resp.getTargetId());
                if (post != null) {
                    resp.setPostTitle(post.getTitle());
                }
            }
        }

        return R.ok(PageRespDto.of(page.getCurrent(), page.getSize(), page.getTotal(), respList));
    }

}




