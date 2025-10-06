package com.xiaou.sensitive.service.impl;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.domain.SensitiveHomophone;
import com.xiaou.sensitive.dto.SensitiveHomophoneQuery;
import com.xiaou.sensitive.engine.TextPreprocessor;
import com.xiaou.sensitive.mapper.SensitiveHomophoneMapper;
import com.xiaou.sensitive.service.SensitiveHomophoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 同音字映射服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveHomophoneServiceImpl implements SensitiveHomophoneService {

    private final SensitiveHomophoneMapper homophoneMapper;
    private final TextPreprocessor textPreprocessor;

    @Override
    public PageResult<SensitiveHomophone> listHomophones(SensitiveHomophoneQuery query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            return homophoneMapper.selectHomophoneList(query);
        });
    }

    @Override
    public SensitiveHomophone getHomophoneById(Integer id) {
        return homophoneMapper.selectHomophoneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addHomophone(SensitiveHomophone homophone) {
        try {
            // 设置默认值
            if (homophone.getStatus() == null) {
                homophone.setStatus(1);
            }

            int result = homophoneMapper.insertHomophone(homophone);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("新增同音字成功: originalChar={}", homophone.getOriginalChar());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("新增同音字失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddHomophones(List<SensitiveHomophone> homophones) {
        try {
            if (homophones == null || homophones.isEmpty()) {
                return false;
            }

            // 设置默认值
            for (SensitiveHomophone homophone : homophones) {
                if (homophone.getStatus() == null) {
                    homophone.setStatus(1);
                }
            }

            int result = homophoneMapper.batchInsertHomophone(homophones);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("批量新增同音字成功，数量: {}", result);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("批量新增同音字失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateHomophone(SensitiveHomophone homophone) {
        try {
            int result = homophoneMapper.updateHomophone(homophone);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("更新同音字成功: id={}", homophone.getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新同音字失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteHomophone(Integer id) {
        try {
            int result = homophoneMapper.deleteHomophoneById(id);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                log.info("删除同音字成功: id={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除同音字失败", e);
            return false;
        }
    }

    @Override
    public void refreshCache() {
        try {
            List<SensitiveHomophone> homophones = homophoneMapper.selectEnabledHomophones();
            textPreprocessor.loadHomophoneMap(homophones);
            log.info("刷新同音字缓存成功");
        } catch (Exception e) {
            log.error("刷新同音字缓存失败", e);
        }
    }
}
