package com.xiaou.log.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.log.domain.bo.SysOperLogBo;
import com.xiaou.log.domain.entity.SysOperLog;
import com.xiaou.log.domain.vo.SysOperLogVo;
import com.xiaou.log.event.OperLogEvent;
import com.xiaou.log.service.ISysOperLogService;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


/**
 * 操作日志记录
 */
@Validated
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController {

    @Resource
    private ISysOperLogService operLogService;

    @GetMapping("/list")
    public R<PageRespDto<SysOperLogVo>> list(@RequestBody PageReqDto req) {
        return operLogService.selectPageOperLogList(req);
    }

}
