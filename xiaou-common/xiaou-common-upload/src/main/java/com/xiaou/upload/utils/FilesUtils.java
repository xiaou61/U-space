package com.xiaou.upload.utils;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.QueryWrapperUtil;

import com.xiaou.upload.domain.FileDetail;
import com.xiaou.upload.mapper.FileDetailMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FilesUtils {


    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileDetailMapper fileDetailMapper;

    /**
     * 上传文件
     *
     * @param file
     * @return
     * @throws Exception
     */
    public FileInfo uploadFile(MultipartFile file) throws Exception {
        FileInfo fileInfo = fileStorageService.of(file)
                .upload();
        return fileInfo;
    }
    /**
     * 批量上传
     */
    public List<FileInfo> uploadFiles(MultipartFile[] files) throws Exception {
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        //遍历
        for (MultipartFile file : files) {
            FileInfo fileInfo = fileStorageService.of(file)
                    .upload();
            fileInfos.add(fileInfo);
        }
        return fileInfos;
    }

    /**
     * 分页查看上传了的文件
     */
    public PageRespDto<FileDetail> list(PageReqDto dto) {
        IPage<FileDetail> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        // 添加排序字段（以 create_time 字段为例）
        QueryWrapper<FileDetail> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));
        IPage<FileDetail> collegeIPage = fileDetailMapper.selectPage(page, queryWrapper);
        return PageRespDto.of(dto.getPageNum(), dto.getPageSize(), collegeIPage.getTotal(), collegeIPage.getRecords());
    }


}
