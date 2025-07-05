package com.xiaou.study.group.teacher.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.study.group.teacher.domain.entity.Homework;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import com.xiaou.study.group.teacher.domain.req.HomeworkApproveReq;
import com.xiaou.study.group.teacher.domain.req.HomeworkReq;
import com.xiaou.study.group.teacher.domain.req.HomeworkSubmissionReq;
import com.xiaou.study.group.teacher.domain.resp.HomeworkResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionGradeResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionResp;

import java.util.List;

public interface HomeworkService extends IService<Homework> {

    R<String> add(HomeworkReq req);

    R<List<HomeworkResp>> listAll(String groupId);

    R<List<HomeworkSubmissionResp>> detail(String id);

    R<String> approve(String id, HomeworkApproveReq req);

    R<HomeworkSubmissionResp> homework(String id);

    R<HomeworkResp> detailHome(String id);

    R<String> updateHomeWork(String id, HomeworkSubmissionReq req);

    R<HomeworkSubmissionGradeResp> homeworkGrade(String id);
}
