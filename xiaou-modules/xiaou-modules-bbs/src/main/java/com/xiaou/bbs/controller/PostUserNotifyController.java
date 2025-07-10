package com.xiaou.bbs.controller;

import com.xiaou.bbs.domain.entity.BbsUserNotify;
import com.xiaou.bbs.domain.resp.BbsUserNotifyResp;
import com.xiaou.bbs.service.BbsUserNotifyService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post/notify")
@Validated
public class PostUserNotifyController {
    @Resource
    private BbsUserNotifyService bbsUserNotifyService;

    /**
     * 查看消息列表分页
     */
    @PostMapping("/list")
    public R<PageRespDto<BbsUserNotifyResp>> PageList(@RequestBody PageReqDto dto){
        return bbsUserNotifyService.pageList(dto);
    }
    /**
     * 点击后就代表所有已读
     */
    @PostMapping("/read")
    public R<String> read(){
        return bbsUserNotifyService.read();
    }


}
