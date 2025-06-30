package com.xiaou.system.log.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

import com.xiaou.upload.domain.FileDetail;
import com.xiaou.upload.utils.FilesUtils;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/file-upload")
public class FileUploadController {

    @Resource
    private FilesUtils filesUtils;

    /**
     * 上传文件后获取url 一个
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping
    public R<String> upload(MultipartFile file) throws Exception {
        FileInfo fileInfo = filesUtils.uploadFile(file);
        return R.ok("上传成功",fileInfo.getUrl());
    }

    @PostMapping("/batch")
    public R<List<String>> uploadBatch(MultipartFile[] files) throws Exception {
        List<String> urls = Arrays.stream(files)
                .map(file -> {
                    try {
                        FileInfo fileInfo = filesUtils.uploadFile(file);
                        return fileInfo.getUrl();
                    } catch (Exception e) {
                        throw new RuntimeException("上传失败: " + file.getOriginalFilename(), e);
                    }
                })
                .toList();
        return R.ok("批量上传成功", urls);
    }

    /**
     * 分页查看已经上传了的文件信息
     */
    @PostMapping("/list")
    public R<PageRespDto<FileDetail>> list(@RequestBody PageReqDto req) {
        return R.ok("查询成功", filesUtils.list(req));
    }
}
