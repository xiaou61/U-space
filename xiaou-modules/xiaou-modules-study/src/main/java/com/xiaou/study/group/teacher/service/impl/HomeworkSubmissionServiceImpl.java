package com.xiaou.study.group.teacher.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import com.xiaou.study.group.teacher.domain.req.HomeworkSubmissionReq;
import com.xiaou.study.group.teacher.mapper.HomeworkSubmissionMapper;
import com.xiaou.study.group.teacher.service.HomeworkSubmissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class HomeworkSubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission>
        implements HomeworkSubmissionService {
    @Resource
    private LoginHelper loginHelper;

    @Override
    public R<String> homework(String id, HomeworkSubmissionReq req) {
        //查看作业是否已经提交过
        QueryWrapper<HomeworkSubmission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("homework_id", id);
        queryWrapper.eq("student_id", loginHelper.getCurrentAppUserId());
        if (this.count(queryWrapper) > 0) {
            return R.fail("作业已经提交过,如需修改可以进行修改重新提交");
        }
        HomeworkSubmission homeworkSubmission = MapstructUtils.convert(req, HomeworkSubmission.class);
        homeworkSubmission.setHomeworkId(id);
        homeworkSubmission.setStudentId(loginHelper.getCurrentAppUserId());

        baseMapper.insert(homeworkSubmission);
        return R.ok("提交成功");
    }
}




