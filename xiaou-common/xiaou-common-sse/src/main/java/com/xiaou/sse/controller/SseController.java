package com.xiaou.sse.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.exception.ServiceException;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.sse.core.SseEmitterManager;
import com.xiaou.sse.dto.SseMessageDto;
import com.xiaou.sse.dto.UserNotifyMessage;
import com.xiaou.sse.manager.SseMessageManager;
import com.xiaou.sse.mapper.UserNotifyMessageMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SSE 控制器
 *
 * @author Lion Li
 */
@RestController
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(SseController.class);
    @Resource
    private LoginHelper loginHelper;


    private final SseEmitterManager sseEmitterManager;

    @Resource
    private SseMessageManager sseMessageManager;

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        String tokenValue = StpUtil.getTokenValue();
        String userId = loginHelper.getCurrentAppUserId();
        // 建立连接
        SseEmitter emitter = sseEmitterManager.connect(userId, tokenValue);
        // 拉取未读
        sseMessageManager.pullAndPushUnreadMessages(userId);

        try {
            List<String> cacheList = RedisUtils.getCacheList(GlobalConstants.USER_ONLINE_KEY);
            //如果id不再缓存中
            //todo 定期要跟数据库进行同步
            if (!cacheList.contains(userId)){
                RedisUtils.addCacheList(GlobalConstants.USER_ONLINE_KEY,userId);
                log.info("用户{}上线",userId);
            }
        } catch (Exception e) {
            // 处理Redis反序列化异常，重新初始化在线用户列表
            log.warn("Redis在线用户列表数据损坏，重新初始化: {}", e.getMessage());
            try {
                RedisUtils.deleteObject(GlobalConstants.USER_ONLINE_KEY);
                RedisUtils.addCacheList(GlobalConstants.USER_ONLINE_KEY, userId);
                log.info("用户{}上线(重新初始化后)", userId);
            } catch (Exception ex) {
                log.error("重新初始化在线用户列表失败", ex);
            }
        }
        return emitter;
    }

    /**
     * 关闭 SSE 连接
     */
    @SaIgnore
    @GetMapping(value = "${sse.path}/close")
    public R<Void> close() {
        String tokenValue = StpUtil.getTokenValue();
        String userId = loginHelper.getCurrentAppUserId();
        sseEmitterManager.disconnect(userId, tokenValue);
        return R.ok();
    }


    @GetMapping(value = "${sse.path}/send")
    public R<Void> send(@RequestBody SseMessageDto sseMessageDto) {
            SseMessageDto dto = new SseMessageDto();
            dto.setUserIds(sseMessageDto.getUserIds());
            dto.setMessage(sseMessageDto.getMessage());
            dto.setType(sseMessageDto.getType());
            sseEmitterManager.publishMessage(dto);
        return R.ok();
    }

    /**
     * 向所有用户发送消息
     */
    @GetMapping(value = "${sse.path}/sendAll")
    public R<Void> sendAll(@RequestBody SseMessageDto SseMessageDto) {
        sseEmitterManager.publishAll(SseMessageDto);

        return R.ok();
    }

    /**
     * 清理资源。此方法目前不执行任何操作，但避免因未实现而导致错误
     */
    @Override
    public void destroy() throws Exception {
        // 销毁时不需要做什么 此方法避免无用操作报错
    }





}
