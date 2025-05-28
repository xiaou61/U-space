package com.xiaou.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.notify.domain.Notification;
import com.xiaou.notify.domain.NotificationVo;
import com.xiaou.notify.mapper.NotificationMapper;
import com.xiaou.notify.service.NotificationService;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.service.StudentUserService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {
    @Resource
    private StudentUserService studentUserService;

    @Override
    public R<PageRespDto<NotificationVo>> getNotifyPage(PageReqDto dto) {
        Long userId = LoginHelper.getCurrentAppUserId();

        // 构建分页对象
        IPage<Notification> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 构建查询条件
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of("create_time"));

        // 查询数据库
        IPage<Notification> notificationIPage = baseMapper.selectPage(page, queryWrapper);

        // 转 VO
        List<NotificationVo> voList = MapstructUtils.convert(notificationIPage.getRecords(), NotificationVo.class);

        // 提取所有 fromUserId 并去重
        Set<Long> fromUserIds = voList.stream()
                .map(NotificationVo::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 查询发送者信息
        Map<Long, StudentUser> userMap = studentUserService.getUserMapByIds(fromUserIds);

        // 填充 name 和 avatar
        voList.forEach(vo -> {
            StudentUser user = userMap.get(vo.getFromUserId());
            if (user != null) {
                vo.setFromUserName(user.getName());
                vo.setFromUserAvatar(user.getAvatarUrl());
            }
        });
        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), notificationIPage.getTotal(), voList));
    }


    @Override
    public R<Integer> getUnreadCount() {
        Long userId = LoginHelper.getCurrentAppUserId();
        long count = lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIs_read, false)
                .count();
        return R.ok((int) count);
    }

    @Override
    public R<String> markAllAsRead() {
        Long userId = LoginHelper.getCurrentAppUserId();
        lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIs_read, false)
                .set(Notification::getIs_read, true)
                .update();
        return R.ok("全部已读");
    }

}




