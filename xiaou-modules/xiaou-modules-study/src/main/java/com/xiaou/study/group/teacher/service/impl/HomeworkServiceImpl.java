package com.xiaou.study.group.teacher.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.Homework;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import com.xiaou.study.group.teacher.domain.req.HomeworkApproveReq;
import com.xiaou.study.group.teacher.domain.req.HomeworkReq;
import com.xiaou.study.group.teacher.domain.req.HomeworkSubmissionReq;
import com.xiaou.study.group.teacher.domain.resp.HomeworkResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionGradeResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionResp;
import com.xiaou.study.group.teacher.mapper.HomeworkMapper;
import com.xiaou.study.group.teacher.mapper.HomeworkSubmissionMapper;
import com.xiaou.study.group.teacher.service.HomeworkService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework>
        implements HomeworkService {

    @Resource
    private LoginHelper loginHelper;


    @Resource
    private HomeworkSubmissionMapper homeworkSubmissionMapper;
    @Resource
    private StudentMapper studentMapper;

    @Override
    public R<String> add(HomeworkReq req) {

        Homework homework = MapstructUtils.convert(req, Homework.class);
        homework.setTeacherId(loginHelper.getCurrentAppUserId());

        baseMapper.insert(homework);
        return R.ok("添加成功");
    }

    @Override
    public R<List<HomeworkResp>> listAll(String groupId) {
        //查询所有的作业
        List<Homework> homeworkList = baseMapper.selectList(new QueryWrapper<Homework>().eq("group_id", groupId));
        List<HomeworkResp> resp = MapstructUtils.convert(homeworkList, HomeworkResp.class);
        return R.ok(resp);
    }

    @Override
    public R<List<HomeworkSubmissionResp>> detail(String id) {
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("homework_id", id);
        List<HomeworkSubmission> homeworkSubmissions = homeworkSubmissionMapper.selectList(queryWrapper);
        List<HomeworkSubmissionResp> convert = MapstructUtils.convert(homeworkSubmissions, HomeworkSubmissionResp.class);
        //填充name
        for (HomeworkSubmissionResp homeworkSubmissionResp : convert) {
            homeworkSubmissionResp.setStudentName(studentMapper.selectById(homeworkSubmissionResp.getStudentId()).getName());
        }
        return R.ok(convert);
    }

    @Override
    public R<String> approve(String id, HomeworkApproveReq req) {
        //查找到那个作业
        HomeworkSubmission homeworkSubmission = homeworkSubmissionMapper.selectById(id);
        homeworkSubmission.setStatus("graded");
        homeworkSubmission.setGrade(req.getGrade());
        homeworkSubmission.setFeedback(req.getFeedback());
        homeworkSubmission.setUpdatedAt(new Date());
        homeworkSubmissionMapper.updateById(homeworkSubmission);
        return R.ok("作业已批改");
    }

    @Override
    public R<HomeworkSubmissionResp> homework(String id) {
        HomeworkSubmission homeworkSubmission = homeworkSubmissionMapper.selectById(id);
        HomeworkSubmissionResp convert = MapstructUtils.convert(homeworkSubmission, HomeworkSubmissionResp.class);
        convert.setStudentName(studentMapper.selectById(convert.getStudentId()).getName());
        return R.ok(convert);
    }

    @Override
    public R<HomeworkResp> detailHome(String id) {
        HomeworkResp convert = MapstructUtils.convert(baseMapper.selectById(id), HomeworkResp.class);
        return R.ok(convert);
    }

    @Override
    public R<String> updateHomeWork(String id, HomeworkSubmissionReq req) {
        //查看是否有这个作业
        Homework homework = baseMapper.selectById(id);
        if (homework == null) {
            return R.fail("作业不存在,请先提交");
        }
        HomeworkSubmission homeworkSubmission = new HomeworkSubmission();
        homeworkSubmission.setContent(req.getContent());
        homeworkSubmission.setAttachmentUrls(req.getAttachmentUrls());
        homeworkSubmission.setUpdatedAt(new Date());
        homeworkSubmissionMapper.updateById(homeworkSubmission);
        return R.ok("修改成功");
    }

    @Override
    public R<HomeworkSubmissionGradeResp> homeworkGrade(String id) {
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("homework_id", id);
        queryWrapper.eq("student_id",loginHelper.getCurrentAppUserId());
        HomeworkSubmission homeworkSubmission = homeworkSubmissionMapper.selectOne(queryWrapper);
        HomeworkSubmissionGradeResp resp = MapstructUtils.convert(homeworkSubmission, HomeworkSubmissionGradeResp.class);
        return R.ok(resp);
    }

}




