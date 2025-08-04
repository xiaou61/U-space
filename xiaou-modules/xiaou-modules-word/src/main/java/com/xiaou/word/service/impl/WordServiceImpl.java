package com.xiaou.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.word.domain.entity.UserWordRecord;
import com.xiaou.word.domain.entity.Word;
import com.xiaou.word.domain.req.WordReq;
import com.xiaou.word.domain.req.WordSelectReq;
import com.xiaou.word.domain.resp.UserWordRecordResp;
import com.xiaou.word.domain.resp.WordBatchResp;
import com.xiaou.word.domain.resp.WordResp;
import com.xiaou.word.mapper.UserWordRecordMapper;
import com.xiaou.word.mapper.WordMapper;
import com.xiaou.word.service.WordService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl extends ServiceImpl<WordMapper, Word>
        implements WordService {
    @Resource
    private WordMapper wordMapper;
    @Resource
    private UserWordRecordMapper userWordRecordMapper;
    @Autowired
    private LoginHelper loginHelper;

    @Override
    public R<String> add(WordReq req) {
        // 判断是否有这个单词
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("word", req.getWord());
        if (wordMapper.selectOne(queryWrapper) != null) {
            return R.fail("单词已存在");
        }
        Word word = MapstructUtils.convert(req, Word.class);
        wordMapper.insert(word);
        return R.ok("添加成功");
    }

    @Override
    public R<WordBatchResp> addBatch(List<WordReq> req) {
        // 判断单词是否存在批量
        List<String> words = req.stream().map(WordReq::getWord).toList();
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("word", words);
        List<Word> existingWords = wordMapper.selectList(queryWrapper);

        List<String> existsWords = existingWords.stream().map(Word::getWord).toList();
        List<WordReq> newWordReqs = req.stream()
                .filter(wordReq -> !existsWords.contains(wordReq.getWord()))
                .toList();

        WordBatchResp wordBatchResp = new WordBatchResp();
        wordBatchResp.setFail(existsWords);

        if (!newWordReqs.isEmpty()) {
            // 只添加不重复的单词
            List<Word> wordList = MapstructUtils.convert(newWordReqs, Word.class);
            for (Word word : wordList) {
                wordMapper.insert(word);
            }
            List<String> successWords = newWordReqs.stream().map(WordReq::getWord).toList();
            wordBatchResp.setSuccess(successWords);
        } else {
            wordBatchResp.setSuccess(List.of());
        }

        return R.ok(wordBatchResp);
    }

    @Override
    public R<String> updateWord(String id, WordReq req) {
        Word word = MapstructUtils.convert(req, Word.class);
        // 根据id查找单词
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        if (wordMapper.selectOne(queryWrapper) == null) {
            return R.fail("单词不存在");
        }
        wordMapper.update(word, queryWrapper);
        return R.ok("更新成功");
    }

    @Override
    public R<PageRespDto<WordResp>> listPage(PageReqDto req) {
        IPage<Word> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        // 定义允许排序的字段
        List<String> validSortFields = List.of("id", "word", "created_at", "updated_at", "difficulty");
        QueryWrapperUtil.applySorting(queryWrapper, req, validSortFields);
        IPage<Word> wordIPage = wordMapper.selectPage(page, queryWrapper);
        List<WordResp> resps = MapstructUtils.convert(wordIPage.getRecords(), WordResp.class);
        return R.ok(PageRespDto.of(req.getPageNum(), req.getPageSize(), wordIPage.getTotal(), resps));
    }

    @Override
    public List<WordResp> exportAllWords() {
        // 查询所有启用状态的单词，按创建时间降序排列
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只导出启用状态的单词
        queryWrapper.orderByDesc("created_at");
        List<Word> words = wordMapper.selectList(queryWrapper);
        return MapstructUtils.convert(words, WordResp.class);
    }

    @Override
    public R<PageRespDto<UserWordRecordResp>> pageUserSelect(PageReqDto req) {
        return getUserWordRecords(req, null);
    }

    @Override
    public R<PageRespDto<UserWordRecordResp>> pageUserSelectKnown(PageReqDto req) {
        return getUserWordRecords(req, "known");
    }

    @Override
    public R<PageRespDto<UserWordRecordResp>> pageUserSelectUnknown(PageReqDto req) {
        return getUserWordRecords(req, "unknown");
    }

    /**
     * 通用的用户单词记录查询方法
     * 
     * @param req        分页请求参数
     * @param filterType 过滤类型：null-全部，"known"-倾向认识，"unknown"-倾向不认识
     */
    private R<PageRespDto<UserWordRecordResp>> getUserWordRecords(PageReqDto req, String filterType) {
        // 分页查询用户学习记录
        IPage<UserWordRecord> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<UserWordRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginHelper.getCurrentAppUserId());

        // 根据过滤类型添加条件
        if ("known".equals(filterType)) {
            // 倾向认识：knowCount > notKnowCount
            queryWrapper.apply("know_count > not_know_count");
        } else if ("unknown".equals(filterType)) {
            // 倾向不认识：notKnowCount > knowCount
            queryWrapper.apply("not_know_count > know_count");
        }

        List<String> validSortFields = List.of("updated_at", "created_at", "last_study_time");
        QueryWrapperUtil.applySorting(queryWrapper, req, validSortFields);

        IPage<UserWordRecord> userWordRecordPage = userWordRecordMapper.selectPage(page, queryWrapper);
        List<UserWordRecord> records = userWordRecordPage.getRecords();

        if (records.isEmpty()) {
            return R.ok(PageRespDto.of(req.getPageNum(), req.getPageSize(), 0L, List.of()));
        }

        // 获取所有单词ID
        List<String> wordIds = records.stream()
                .map(UserWordRecord::getWordId)
                .collect(Collectors.toList());

        // 批量查询单词信息
        QueryWrapper<Word> wordQueryWrapper = new QueryWrapper<>();
        wordQueryWrapper.in("id", wordIds);
        List<Word> words = wordMapper.selectList(wordQueryWrapper);

        // 构建单词ID到单词名称的映射
        Map<String, String> wordMap = words.stream()
                .collect(Collectors.toMap(Word::getId, Word::getWord));

        // 组装响应数据
        List<UserWordRecordResp> respList = records.stream()
                .map(record -> {
                    UserWordRecordResp resp = MapstructUtils.convert(record, UserWordRecordResp.class);
                    resp.setWord(wordMap.get(record.getWordId()));
                    return resp;
                })
                .collect(Collectors.toList());

        return R.ok(PageRespDto.of(req.getPageNum(), req.getPageSize(),
                userWordRecordPage.getTotal(), respList));
    }
}
