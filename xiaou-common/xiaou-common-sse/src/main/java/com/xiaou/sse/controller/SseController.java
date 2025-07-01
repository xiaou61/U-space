package com.xiaou.sse.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.sse.core.SseEmitterManager;
import com.xiaou.sse.dto.SseMessageDto;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * SSE 控制器
 *
 * @author Lion Li
 */
@RestController
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    @Resource
    private LoginHelper loginHelper;

    private final SseEmitterManager sseEmitterManager;

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        String tokenValue = StpUtil.getTokenValue();
        String userId = loginHelper.getCurrentAppUserId();
        return sseEmitterManager.connect(userId, tokenValue);
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

    /**
     * 向特定用户发送消息
     *
     * @param userId 目标用户的 ID
     * @param msg    要发送的消息内容
     */
    @GetMapping(value = "${sse.path}/send")
    public R<Void> send(String userId, String msg) {
        SseMessageDto dto = new SseMessageDto();
        dto.setUserIds(List.of(userId));
        dto.setMessage(msg);
        sseEmitterManager.publishMessage(dto);
        return R.ok();
    }

    /**
     * 向所有用户发送消息
     *
     * @param msg 要发送的消息内容
     */
    @GetMapping(value = "${sse.path}/sendAll")
    public R<Void> send(String msg) {
        sseEmitterManager.publishAll(msg);
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
