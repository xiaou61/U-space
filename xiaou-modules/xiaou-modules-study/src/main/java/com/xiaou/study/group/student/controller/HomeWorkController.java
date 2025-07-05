package com.xiaou.study.group.student.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.exception.ServiceException;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import com.xiaou.study.group.teacher.domain.req.HomeworkReq;
import com.xiaou.study.group.teacher.domain.req.HomeworkSubmissionReq;
import com.xiaou.study.group.teacher.domain.resp.HomeworkResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionGradeResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionResp;
import com.xiaou.study.group.teacher.service.HomeworkService;
import com.xiaou.study.group.teacher.service.HomeworkSubmissionService;
import com.xiaou.upload.utils.FilesUtils;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/student/homework")
@Validated
public class HomeWorkController {
    @Resource
    private HomeworkService homeworkService;
    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private FilesUtils filesUtils;


    /**
     * 查看群组作业列表
     */
    @PostMapping("/list")
    public R<List<HomeworkResp>> list(@RequestParam String groupId) {
        return homeworkService.listAll(groupId);
    }

    /**
     * 查询某个作业详细
     */
    @PostMapping("/homework-detail")
    public R<HomeworkResp> detailHome(@RequestParam String id) {
        return homeworkService.detailHome(id);
    }
    /**
     * 查看是否评分
     */
    @PostMapping("/homeworkgrade")
    public R<HomeworkSubmissionGradeResp> homeworkgrade(@RequestParam String id) {
        return homeworkService.homeworkGrade(id);
    }


    /**
     * 提交一个作业
     */
    @PostMapping("/homework")
    public R<String> homework(@RequestParam String id, @RequestBody HomeworkSubmissionReq req) {
        return homeworkSubmissionService.homework(id, req);
    }
    /**
     * 修改一个作业
     */
    @PostMapping("/update")
    public R<String> update(@RequestParam String id, @RequestBody HomeworkSubmissionReq req) {
        return homeworkService.updateHomeWork(id,req);
    }

    /**
     * 上传附件
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            FileInfo fileInfo = filesUtils.uploadFile(file);
            return R.ok("上传成功",fileInfo.getUrl());
        } catch (Exception e) {
            throw new ServiceException("上传附件失败");
        }
    }
}
