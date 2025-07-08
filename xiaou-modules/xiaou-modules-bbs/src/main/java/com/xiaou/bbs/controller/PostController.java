package com.xiaou.bbs.controller;

import com.xiaou.bbs.domain.req.BbsPostReq;
import com.xiaou.bbs.domain.resp.BbsPostResp;
import com.xiaou.bbs.service.BbsPostService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/post")
@Validated
public class PostController {

    @Resource
    private BbsPostService bbsPostService;

    /**
     * 新增帖子
     */
    @PostMapping("/add")
    public R<String> addPost(@RequestBody BbsPostReq req) {
        return bbsPostService.add(req);
    }

    /**
     * 删除自己帖子
     */
    @PostMapping("/delete")
    public R<String> deletePost(@RequestBody String id) {
        return bbsPostService.delete(id);
    }

    /**
     * 分页查看指定类型的帖子
     */
    @PostMapping("/list")
    public R<PageRespDto<BbsPostResp>> listPost(@RequestParam String categoryId, @RequestBody PageReqDto reqDto) {
        return bbsPostService.listPost(categoryId, reqDto);
    }

    /**
     * 分页查看全部帖子
     */
    @PostMapping("/listAll")
    public R<PageRespDto<BbsPostResp>> listAllPost(@RequestBody PageReqDto reqDto) {
        return bbsPostService.listPost(null, reqDto);
    }
    /**
     * 查看我发布的帖子
     */
    @GetMapping("/listMine")
    public R<PageRespDto<BbsPostResp>> listMinePost(@RequestBody PageReqDto reqDto) {
        return bbsPostService.listPostMy(reqDto);
    }
    /**
     * 上传帖子图片
     */
    @PostMapping("/upload")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return bbsPostService.uploadImage(file);
    }

}
