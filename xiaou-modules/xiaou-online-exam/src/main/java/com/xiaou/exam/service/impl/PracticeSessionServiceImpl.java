package com.xiaou.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.exam.domain.bo.PracticeStartBo;
import com.xiaou.exam.domain.bo.SubmitAnswerBo;
import com.xiaou.exam.domain.entity.ExamRepo;
import com.xiaou.exam.domain.entity.PracticeQuestionRecord;
import com.xiaou.exam.domain.entity.PracticeSession;
import com.xiaou.exam.domain.entity.Question;
import com.xiaou.exam.domain.vo.PracticeQuestionRecordVo;
import com.xiaou.exam.domain.vo.PracticeRecordVo;
import com.xiaou.exam.domain.vo.PracticeSessionVo;
import com.xiaou.exam.domain.vo.QuestionVo;
import com.xiaou.exam.mapper.ExamRepoMapper;
import com.xiaou.exam.mapper.PracticeSessionMapper;
import com.xiaou.exam.mapper.QuestionMapper;
import com.xiaou.exam.service.PracticeQuestionRecordService;
import com.xiaou.exam.service.PracticeSessionService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;


@Service
public class PracticeSessionServiceImpl extends ServiceImpl<PracticeSessionMapper, PracticeSession>
        implements PracticeSessionService {
    @Resource
    private ExamRepoMapper examRepoMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private PracticeQuestionRecordService practiceQuestionRecordService;


    @Override
    public R<PracticeSessionVo> startPractice(PracticeStartBo startBo) {
        Long userId = LoginHelper.getCurrentAppUserId();
        // 1. 检查题库是否存在 & 是否支持练习
        ExamRepo repo = examRepoMapper.selectById(startBo.getRepoId());
        if (repo == null || repo.getIsDeleted() == 1 || repo.getIsExercise() == 0) {
            return R.fail("题库不存在或不支持练习");
        }
        // 2. 查询题库下所有题目
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getRepoId, startBo.getRepoId())
                .eq(Question::getIsDeleted, 0));

        if (questions.isEmpty()) {
            return R.fail("该题库暂无可练习的题目");
        }
        // 3. 抽题（全部 or 部分）
        int count = startBo.getQuestionCount() != null
                ? Math.min(startBo.getQuestionCount(), questions.size())
                : questions.size();

        Collections.shuffle(questions); // 打乱
        List<Question> selectedQuestions = questions.subList(0, count);

        // 4. 创建练习会话记录
        PracticeSession session = new PracticeSession();
        session.setUserId(userId);
        session.setRepoId(startBo.getRepoId());
        session.setQuestionCount(count);
        session.setCorrectCount(0);
        session.setWrongCount(0);
        session.setStartTime(new Date());
        session.setEndTime(null);

        this.save(session); // 插入后，session.id 会自动回填
        // 5. 插入每一道题的记录（未答题）
        List<PracticeQuestionRecord> records = selectedQuestions.stream().map(q -> {
            PracticeQuestionRecord record = new PracticeQuestionRecord();
            record.setSessionId(session.getId());
            record.setQuestionId(q.getId());
            record.setUserAnswer(null);
            record.setIsCorrect(null); // 尚未答题
            return record;
        }).toList();
        practiceQuestionRecordService.saveBatch(records);
        // 6. 转 VO 返回
        PracticeSessionVo vo = new PracticeSessionVo();
        vo.setSessionId(session.getId());
        vo.setRepoId(session.getRepoId());
        vo.setQuestionCount(session.getQuestionCount());
        vo.setCorrectCount(0);
        vo.setWrongCount(0);
        vo.setStartTime(session.getStartTime());
        return R.ok(vo);
    }

    @Override
    public R<QuestionVo> getNextQuestion(Long sessionId) {
        PracticeSession session = this.getById(sessionId);
        if (session == null) {
            return R.fail("练习会话不存在");
        }

        // 1. 查未答过的题目
        List<PracticeQuestionRecord> unAnswered = practiceQuestionRecordService.list(
                new LambdaQueryWrapper<PracticeQuestionRecord>()
                        .eq(PracticeQuestionRecord::getSessionId, sessionId)
                        .isNull(PracticeQuestionRecord::getIsCorrect) // 未答过
                        .orderByAsc(PracticeQuestionRecord::getId)
        );

        List<PracticeQuestionRecord> chosenList;
        int currentIndex = session.getCurrentIndex() == null ? 0 : session.getCurrentIndex();

        if (!unAnswered.isEmpty()) {
            // 2. 还有未答过的，返回未答题中currentIndex对应的题
            if (currentIndex >= unAnswered.size()) currentIndex = 0;
            chosenList = unAnswered;
        } else {
            // 3. 全部答过，查询已答题目
            List<PracticeQuestionRecord> answered = practiceQuestionRecordService.list(
                    new LambdaQueryWrapper<PracticeQuestionRecord>()
                            .eq(PracticeQuestionRecord::getSessionId, sessionId)
                            .isNotNull(PracticeQuestionRecord::getIsCorrect)
                            .orderByAsc(PracticeQuestionRecord::getId)
            );
            if (answered.isEmpty()) {
                return R.fail("没有题目");
            }
            if (currentIndex >= answered.size()) currentIndex = 0;
            chosenList = answered;
        }

        PracticeQuestionRecord record = chosenList.get(currentIndex);
        Question question = questionMapper.selectById(record.getQuestionId());
        if (question == null) {
            return R.fail("题目不存在");
        }

        // 4. 更新索引，为下次调用做准备
        session.setCurrentIndex(currentIndex + 1);
        this.updateById(session);

        QuestionVo vo = MapstructUtils.convert(question, QuestionVo.class);
        return R.ok(vo);
    }


    @Override
    public R<String> submitAnswer(SubmitAnswerBo submitBo) {
        Long sessionId = submitBo.getSessionId();
        Long questionId = submitBo.getQuestionId();
        String userAnswer = submitBo.getUserAnswer();

        // 1. 验证练习会话是否存在
        PracticeSession session = this.getById(sessionId);
        if (session == null) {
            return R.fail("练习会话不存在");
        }

        // 2. 查询该会话的答题记录
        PracticeQuestionRecord record = practiceQuestionRecordService.getOne(
                new LambdaQueryWrapper<PracticeQuestionRecord>()
                        .eq(PracticeQuestionRecord::getSessionId, sessionId)
                        .eq(PracticeQuestionRecord::getQuestionId, questionId)
        );
        if (record == null) {
            return R.fail("答题记录不存在");
        }

        // 3. 查询题目信息，获取正确答案
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return R.fail("题目不存在");
        }

        // 4. 判断用户答案是否正确（简单字符串比较，可根据题型自定义）
        boolean isCorrect = userAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());

        // 5. 更新答题记录
        record.setUserAnswer(userAnswer);
        record.setIsCorrect(isCorrect ? 1 : 0);
        practiceQuestionRecordService.updateById(record);

        // 6. 更新练习会话的正确数和错误数（需先查询统计）
        Long correctCount = practiceQuestionRecordService.count(
                new LambdaQueryWrapper<PracticeQuestionRecord>()
                        .eq(PracticeQuestionRecord::getSessionId, sessionId)
                        .eq(PracticeQuestionRecord::getIsCorrect, 1)
        );
        Long wrongCount = practiceQuestionRecordService.count(
                new LambdaQueryWrapper<PracticeQuestionRecord>()
                        .eq(PracticeQuestionRecord::getSessionId, sessionId)
                        .eq(PracticeQuestionRecord::getIsCorrect, 0)
        );
        session.setCorrectCount(Math.toIntExact(correctCount));
        session.setWrongCount(Math.toIntExact(wrongCount));
        this.updateById(session);

        return R.ok("答案提交成功");
    }


    @Override
    public R<PracticeRecordVo> getPracticeRecord(Long sessionId) {
        // 1. 查询练习会话
        PracticeSession session = this.getById(sessionId);
        if (session == null) {
            return R.fail("练习会话不存在");
        }

        // 2. 查询该会话所有答题记录
        List<PracticeQuestionRecord> records = practiceQuestionRecordService.list(
                new LambdaQueryWrapper<PracticeQuestionRecord>()
                        .eq(PracticeQuestionRecord::getSessionId, sessionId)
                        .orderByAsc(PracticeQuestionRecord::getId)
        );

        // 3. 查询对应题目信息，组装答题详情
        List<PracticeQuestionRecordVo> recordVos = records.stream().map(record -> {
            PracticeQuestionRecordVo vo = new PracticeQuestionRecordVo();
            vo.setQuestionId(record.getQuestionId());
            vo.setUserAnswer(record.getUserAnswer());
            vo.setIsCorrect(record.getIsCorrect() != null && record.getIsCorrect() == 1);

            Question question = questionMapper.selectById(record.getQuestionId());
            if (question != null) {
                vo.setQuestionContent(question.getContent());
                vo.setCorrectAnswer(question.getCorrectAnswer());
                vo.setType(question.getType());
            }
            return vo;
        }).toList();

        // 4. 构造PracticeRecordVo响应
        PracticeRecordVo result = MapstructUtils.convert(session, PracticeRecordVo.class);
        result.setRecords(recordVos);

        return R.ok(result);
    }

    @Override
    public R<List<PracticeSessionVo>> getPracticeHistory() {
        // 1. 获取当前用户ID，假设用Sa-Token
        Long userId = LoginHelper.getCurrentAppUserId();

        // 2. 查询该用户所有练习会话（未删除的）
        List<PracticeSession> sessions = this.list(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getUserId, userId)
                .orderByDesc(PracticeSession::getStartTime)
        );

        // 3. 转换成 PracticeSessionVo 列表
        List<PracticeSessionVo> voList = MapstructUtils.convert(sessions, PracticeSessionVo.class);
        // 4. 返回结果
        return R.ok(voList);
    }

}




