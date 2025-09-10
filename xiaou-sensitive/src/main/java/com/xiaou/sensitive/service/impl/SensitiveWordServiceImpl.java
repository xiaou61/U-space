package com.xiaou.sensitive.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.api.SensitiveCheckService;
import com.xiaou.sensitive.domain.SensitiveCategory;
import com.xiaou.sensitive.domain.SensitiveWord;
import com.xiaou.sensitive.dto.SensitiveWordDTO;
import com.xiaou.sensitive.dto.SensitiveWordQuery;
import com.xiaou.sensitive.mapper.SensitiveCategoryMapper;
import com.xiaou.sensitive.mapper.SensitiveWordMapper;
import com.xiaou.sensitive.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveCategoryMapper sensitiveCategoryMapper;
    private final SensitiveCheckService sensitiveCheckService;

    @Override
    public PageResult<SensitiveWordDTO> listWords(SensitiveWordQuery query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            return sensitiveWordMapper.selectWordList(query);
        });
    }

    @Override
    public SensitiveWord getWordById(Long id) {
        return sensitiveWordMapper.selectWordById(id);
    }

    @Override
    @Transactional
    public boolean addWord(SensitiveWord word) {
        try {
            // 检查敏感词是否已存在
            SensitiveWord existingWord = sensitiveWordMapper.selectWordByWord(word.getWord());
            if (existingWord != null) {
                log.warn("敏感词已存在：{}", word.getWord());
                return false;
            }

            // 设置默认值
            if (word.getLevel() == null) {
                word.setLevel(1);
            }
            if (word.getAction() == null) {
                word.setAction(1);
            }
            if (word.getStatus() == null) {
                word.setStatus(1);
            }

            int result = sensitiveWordMapper.insertWord(word);
            
            if (result > 0) {
                // 刷新敏感词库
                sensitiveCheckService.refreshWordLibrary();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("新增敏感词失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateWord(SensitiveWord word) {
        try {
            int result = sensitiveWordMapper.updateWord(word);
            
            if (result > 0) {
                // 刷新敏感词库
                sensitiveCheckService.refreshWordLibrary();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("更新敏感词失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteWord(Long id) {
        try {
            int result = sensitiveWordMapper.deleteWordById(id);
            
            if (result > 0) {
                // 刷新敏感词库
                sensitiveCheckService.refreshWordLibrary();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("删除敏感词失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteWords(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }

            int result = sensitiveWordMapper.deleteWordByIds(ids);
            
            if (result > 0) {
                // 刷新敏感词库
                sensitiveCheckService.refreshWordLibrary();
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("批量删除敏感词失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public ImportResult importWords(List<String> words, Long creatorId) {
        int total = words.size();
        int success = 0;
        int duplicate = 0;
        List<String> errors = new ArrayList<>();

        try {
            List<SensitiveWord> validWords = new ArrayList<>();

            for (String wordText : words) {
                if (StrUtil.isBlank(wordText)) {
                    continue;
                }

                wordText = wordText.trim();
                
                // 检查是否已存在
                SensitiveWord existingWord = sensitiveWordMapper.selectWordByWord(wordText);
                if (existingWord != null) {
                    duplicate++;
                    continue;
                }

                // 创建敏感词对象
                SensitiveWord word = new SensitiveWord();
                word.setWord(wordText);
                word.setCategoryId(3); // 默认分类：人身攻击
                word.setLevel(1);      // 默认风险等级：低
                word.setAction(1);     // 默认动作：替换
                word.setStatus(1);     // 默认状态：启用
                word.setCreatorId(creatorId);

                validWords.add(word);
            }

            // 批量插入
            if (!validWords.isEmpty()) {
                int result = sensitiveWordMapper.batchInsertWords(validWords);
                success = result;
            }

            // 刷新敏感词库
            if (success > 0) {
                sensitiveCheckService.refreshWordLibrary();
            }

        } catch (Exception e) {
            log.error("批量导入敏感词失败：{}", e.getMessage(), e);
            errors.add("导入过程中发生异常：" + e.getMessage());
        }

        return new ImportResult(total, success, duplicate, errors);
    }

    @Override
    public List<SensitiveCategory> listCategories() {
        return sensitiveCategoryMapper.selectAllCategories();
    }
} 