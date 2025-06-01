package com.xiaou.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.exam.domain.bo.QuestionBo;
import com.xiaou.exam.domain.entity.ExamRepo;
import com.xiaou.exam.domain.entity.Question;
import com.xiaou.exam.domain.entity.QuestionOption;
import com.xiaou.exam.domain.vo.QuestionCategoryVo;
import com.xiaou.exam.domain.vo.QuestionVo;
import com.xiaou.exam.mapper.ExamRepoMapper;
import com.xiaou.exam.mapper.QuestionMapper;
import com.xiaou.exam.mapper.QuestionOptionMapper;
import com.xiaou.exam.service.QuestionOptionService;
import com.xiaou.exam.service.QuestionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {
    @Resource
    private ExamRepoMapper examRepoMapper;
    @Resource
    private QuestionOptionMapper optionMapper;

    @Resource
    private QuestionOptionService optionService;

    @Override
    @Transactional
    public R<String> addQuestion(QuestionBo questionBo) {
        // 校验选项数量
        List<QuestionOption> options = questionBo.getOptions();
        int type = questionBo.getType();

        if (type != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return R.fail("非简答题的试题选项不能少于两个");
        }
        if (type == 4 && (Objects.isNull(options) || options.size() < 1)) {
            return R.fail("简答题至少需要一个答案内容");
        }

        // 转换对象并设置分类ID
        Question question = MapstructUtils.convert(questionBo, Question.class);
        ExamRepo repo = examRepoMapper.selectById(question.getRepoId());
        if (repo == null) {
            return R.fail("题库不存在");
        }
        question.setCategoryId(repo.getCategoryId());

        // 保存题目
        boolean saved = save(question);
        if (!saved) {
            return R.fail("试题保存失败");
        }

        // 设置选项关联题目ID
        Long questionId = question.getId();
        options.forEach(opt -> opt.setQuestionId(questionId));

        // 保存选项
        if (type == 4) {
            //todo 目前简答题只有一个答案 后续可以修改
            optionMapper.insert(options.get(0));
        } else {
            optionService.saveBatch(options);
        }

        // 更新题库的试题数量 +1
        examRepoMapper.incrementQuestionCount(question.getRepoId());


        return R.ok("添加成功");
    }

    @Override
    @Transactional
    public R<String> deleteQuestions(String ids) {
        List<Long> qIdList = Arrays.stream(ids.split(","))
                .map(id -> id.replaceAll("\"", "").trim()) // 去除引号和空格
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 查询这些题目的 repo_id 列表
        List<Question> questionList = baseMapper.selectBatchIds(qIdList);
        if (CollectionUtils.isEmpty(questionList)) {
            return R.fail("未找到对应的试题");
        }

        // 统计每个题库下删除题目的数量
        Map<Long, Long> repoIdToCountMap = questionList.stream()
                .collect(Collectors.groupingBy(Question::getRepoId, Collectors.counting()));

        // 删除选项
        optionMapper.deleteBatchIds(qIdList);

        // 删除试题
        baseMapper.deleteBatchIds(qIdList);

        // 更新对应题库的 question_count
        repoIdToCountMap.forEach((repoId, count) -> {
            examRepoMapper.decrementQuestionCount(repoId, count.intValue());
        });

        return R.ok("批量删除成功");
    }

    @Override
    public R<QuestionVo> get(Long id) {
        // 1. 查询试题基本信息
        Question question = baseMapper.selectById(id);
        if (question == null) {
            return R.fail("试题不存在");
        }
        // 2. 查询该试题对应的选项列表
        List<QuestionOption> options = optionMapper.selectList(
                new QueryWrapper<QuestionOption>().eq("question_id", id).eq("is_deleted", 0)
        );
        QuestionVo questionVo = MapstructUtils.convert(question, QuestionVo.class);
        questionVo.setOptions(options);
        return R.ok(questionVo);
    }

    @Override
    public R<List<QuestionVo>> list(Long repoId) {
        // 1. 查询该题库下所有试题（未删除）
        List<Question> questions = baseMapper.selectList(
                new QueryWrapper<Question>()
                        .eq("repo_id", repoId)
                        .eq("is_deleted", 0)
        );

        if (questions.isEmpty()) {
            return R.ok(Collections.emptyList());
        }

        // 2. 批量查询所有题目的选项，避免N+1问题
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        List<QuestionOption> options = optionMapper.selectList(
                new QueryWrapper<QuestionOption>()
                        .in("question_id", questionIds)
                        .eq("is_deleted", 0)
        );

        // 3. 按题目ID分组选项
        Map<Long, List<QuestionOption>> optionsMap = options.stream()
                .collect(Collectors.groupingBy(QuestionOption::getQuestionId));
        List<QuestionVo> result = questions.stream().map(q -> {
            QuestionVo questionVo = MapstructUtils.convert(q, QuestionVo.class);
            questionVo.setOptions(optionsMap.getOrDefault(q.getId(), Collections.emptyList()));
            return questionVo;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    @Override
    public R<List<Long>> ids(Long repoId) {
        List<Long> ids = baseMapper.selectList(
                new QueryWrapper<Question>()
                        .eq("repo_id", repoId)
                        .eq("is_deleted", 0)
        ).stream().map(Question::getId).collect(Collectors.toList());
         return R.ok(ids);
    }

}




