package com.xiaou.sensitive.service.impl;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.domain.SensitiveSimilarChar;
import com.xiaou.sensitive.dto.SensitiveSimilarCharQuery;
import com.xiaou.sensitive.engine.TextPreprocessor;
import com.xiaou.sensitive.mapper.SensitiveSimilarCharMapper;
import com.xiaou.sensitive.service.SensitiveSimilarCharService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 形似字映射服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveSimilarCharServiceImpl implements SensitiveSimilarCharService {

    private final SensitiveSimilarCharMapper similarCharMapper;
    private final TextPreprocessor textPreprocessor;

    @Override
    public PageResult<SensitiveSimilarChar> listSimilarChars(SensitiveSimilarCharQuery query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            return similarCharMapper.selectSimilarCharList(query);
        });
    }

    @Override
    public SensitiveSimilarChar getSimilarCharById(Integer id) {
        return similarCharMapper.selectSimilarCharById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSimilarChar(SensitiveSimilarChar similarChar) {
        try {
            // 设置默认值
            if (similarChar.getStatus() == null) {
                similarChar.setStatus(1);
            }

            int result = similarCharMapper.insertSimilarChar(similarChar);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("新增形似字成功: originalChar={}", similarChar.getOriginalChar());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("新增形似字失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddSimilarChars(List<SensitiveSimilarChar> similarChars) {
        try {
            if (similarChars == null || similarChars.isEmpty()) {
                return false;
            }

            // 设置默认值
            for (SensitiveSimilarChar similarChar : similarChars) {
                if (similarChar.getStatus() == null) {
                    similarChar.setStatus(1);
                }
            }

            int result = similarCharMapper.batchInsertSimilarChar(similarChars);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("批量新增形似字成功，数量: {}", result);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("批量新增形似字失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSimilarChar(SensitiveSimilarChar similarChar) {
        try {
            int result = similarCharMapper.updateSimilarChar(similarChar);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("更新形似字成功: id={}", similarChar.getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新形似字失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSimilarChar(Integer id) {
        try {
            int result = similarCharMapper.deleteSimilarCharById(id);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("删除形似字成功: id={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除形似字失败", e);
            return false;
        }
    }

    @Override
    public void refreshCache() {
        try {
            List<SensitiveSimilarChar> similarChars = similarCharMapper.selectEnabledSimilarChars();
            textPreprocessor.loadSimilarCharMap(similarChars);
            log.info("刷新形似字缓存成功");
        } catch (Exception e) {
            log.error("刷新形似字缓存失败", e);
        }
    }
}
