package com.xiaou.study.group.teacher.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import com.xiaou.study.group.teacher.domain.req.HomeworkSubmissionReq;

public interface HomeworkSubmissionService extends IService<HomeworkSubmission> {

    R<String> homework(String id, HomeworkSubmissionReq req);
}
