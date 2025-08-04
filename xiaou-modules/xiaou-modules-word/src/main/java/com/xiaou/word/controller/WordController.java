package com.xiaou.word.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.word.domain.entity.UserWordRecord;
import com.xiaou.word.domain.req.WordSelectReq;
import com.xiaou.word.domain.resp.UserWordRecordResp;
import com.xiaou.word.domain.resp.WordResp;
import com.xiaou.word.service.UserWordRecordService;
import com.xiaou.word.service.WordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/word")
@Validated
public class WordController {
    @Resource
    private WordService wordService;

    @Resource
    private UserWordRecordService userWordRecordService;

    /**
     * 分页查看单词 用户接口
     */
    @PostMapping("/page")
    public R<PageRespDto<WordResp>> page(@RequestBody PageReqDto req) {
        return wordService.listPage(req);
    }

    /**
     * 用户对单词选择认识or不认识
     */
    @PostMapping("/select")
    public R<String> select(@RequestBody WordSelectReq req) {
        return userWordRecordService.select(req);
    }

    /**
     * 分页查看用户对单词的操作
     */
    @PostMapping("/pageUserSelect")
    public R<PageRespDto<UserWordRecordResp>> pageUserSelect(@RequestBody PageReqDto req) {
        return wordService.pageUserSelect(req);
    }

    /**
     * 分页查看用户倾向认识的单词
     */
    @PostMapping("/pageUserSelectKnown")
    public R<PageRespDto<UserWordRecordResp>> pageUserSelectKnown(@RequestBody PageReqDto req) {
        return wordService.pageUserSelectKnown(req);
    }

    /**
     * 分页查看用户倾向不认识的单词
     */
    @PostMapping("/pageUserSelectUnknown")
    public R<PageRespDto<UserWordRecordResp>> pageUserSelectUnknown(@RequestBody PageReqDto req) {
        return wordService.pageUserSelectUnknown(req);
    }
}
