package com.xiaou.system.log.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

import com.xiaou.system.log.domain.vo.SysOperLogVo;
import com.xiaou.system.log.service.ISysOperLogService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 操作日志记录
 */
@Validated
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController {

    @Resource
    private ISysOperLogService operLogService;

    @PostMapping("/list")
    public R<PageRespDto<SysOperLogVo>> list(@RequestBody PageReqDto req) {
        return operLogService.selectPageOperLogList(req);
    }

}
