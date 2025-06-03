package com.xiaou.exam.controller;

import com.xiaou.common.domain.R;
import com.xiaou.exam.domain.bo.PracticeStartBo;
import com.xiaou.exam.domain.bo.SubmitAnswerBo;
import com.xiaou.exam.domain.vo.PracticeRecordVo;
import com.xiaou.exam.domain.vo.PracticeSessionVo;
import com.xiaou.exam.domain.vo.QuestionVo;
import com.xiaou.exam.service.PracticeSessionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/practice")
@Validated
public class PracticeController {
    @Resource
    private PracticeSessionService practiceSessionService;

    /**
     * 开始新的练习
     */
    @PostMapping("/start")
    public R<PracticeSessionVo> startPractice(@RequestBody @Valid PracticeStartBo startBo) {
        return practiceSessionService.startPractice(startBo);
    }

    /**
     * 获取下一题（乱序，先未答题，全部答完后可重复答）
     */
    @GetMapping("/nextQuestion/{sessionId}")
    public R<QuestionVo> getNextQuestion(@PathVariable Long sessionId) {
        return practiceSessionService.getNextQuestion(sessionId);
    }

    /**
     * 提交答案
     */
    @PostMapping("/submitAnswer")
    public R<String> submitAnswer(@RequestBody SubmitAnswerBo submitBo) {
        return practiceSessionService.submitAnswer(submitBo);
    }

    /**
     * 查询练习详情（答题记录）
     */
    @GetMapping("/record/{sessionId}")
    public R<PracticeRecordVo> getPracticeRecord(@PathVariable Long sessionId) {
        return practiceSessionService.getPracticeRecord(sessionId);
    }

    /**
     * 获取当前用户练习历史列表
     */
    @GetMapping("/history")
    public R<List<PracticeSessionVo>> getPracticeHistory() {
        return practiceSessionService.getPracticeHistory();
    }
}
