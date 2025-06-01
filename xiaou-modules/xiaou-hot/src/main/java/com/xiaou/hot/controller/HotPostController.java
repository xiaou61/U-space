package com.xiaou.hot.controller;

import com.xiaou.common.domain.R;
import com.xiaou.hot.model.vo.HotPostVO;
import com.xiaou.hot.service.HotPostService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/hot")
@Slf4j
@RequiredArgsConstructor
public class HotPostController {

    private final HotPostService hotPostService;

    /**
     * 获取列表（封装类）
     */
    @PostMapping("/list")
    public R<List<HotPostVO>> getHotPostList() {
        return R.ok(hotPostService.getHotPostList());

    }


}