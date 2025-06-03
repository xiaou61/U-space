package com.xiaou.exam.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.PracticeStartBo;
import com.xiaou.exam.domain.bo.SubmitAnswerBo;
import com.xiaou.exam.domain.entity.PracticeSession;
import com.xiaou.exam.domain.vo.PracticeRecordVo;
import com.xiaou.exam.domain.vo.PracticeSessionVo;
import com.xiaou.exam.domain.vo.QuestionVo;

import java.util.List;

public interface PracticeSessionService extends IService<PracticeSession> {

    R<PracticeSessionVo> startPractice(PracticeStartBo startBo);

    R<QuestionVo> getNextQuestion(Long sessionId);

    R<String> submitAnswer(SubmitAnswerBo submitBo);

    R<PracticeRecordVo> getPracticeRecord(Long sessionId);

    R<List<PracticeSessionVo>> getPracticeHistory();
}
