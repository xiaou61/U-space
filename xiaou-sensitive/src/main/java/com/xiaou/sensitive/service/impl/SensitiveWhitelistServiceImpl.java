package com.xiaou.sensitive.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.domain.SensitiveWhitelist;
import com.xiaou.sensitive.dto.SensitiveWhitelistQuery;
import com.xiaou.sensitive.mapper.SensitiveWhitelistMapper;
import com.xiaou.sensitive.service.SensitiveWhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 敏感词白名单服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWhitelistServiceImpl implements SensitiveWhitelistService {

    private final SensitiveWhitelistMapper whitelistMapper;

    /**
     * 白名单缓存（Caffeine本地缓存）
     */
    private Cache<String, Boolean> cache;

    @PostConstruct
    public void init() {
        // 初始化本地缓存
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
        
        // 预加载白名单
        refreshCache();
    }

    @Override
    public PageResult<SensitiveWhitelist> listWhitelist(SensitiveWhitelistQuery query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            return whitelistMapper.selectWhitelistList(query);
        });
    }

    @Override
    public SensitiveWhitelist getWhitelistById(Long id) {
        return whitelistMapper.selectWhitelistById(id);
    }

    @Override
    public boolean isInWhitelist(String word, String module) {
        if (StrUtil.isBlank(word)) {
            return false;
        }

        // 构建缓存Key
        String cacheKey = buildCacheKey(word, module);

        // 从缓存获取
        Boolean result = cache.get(cacheKey, key -> {
            // 缓存未命中，从数据库查询
            Integer count;
            if (StrUtil.isNotBlank(module)) {
                // 先查模块白名单，再查全局白名单
                count = whitelistMapper.existsInWhitelist(word, "module", module);
                if (count != null && count > 0) {
                    return true;
                }
            }
            // 查全局白名单
            count = whitelistMapper.existsInWhitelist(word, "global", null);
            return count != null && count > 0;
        });

        return Boolean.TRUE.equals(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addWhitelist(SensitiveWhitelist whitelist) {
        try {
            // 设置默认值
            if (whitelist.getStatus() == null) {
                whitelist.setStatus(1);
            }
            if (StrUtil.isBlank(whitelist.getScope())) {
                whitelist.setScope("global");
            }

            int result = whitelistMapper.insertWhitelist(whitelist);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("新增白名单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWhitelist(SensitiveWhitelist whitelist) {
        try {
            int result = whitelistMapper.updateWhitelist(whitelist);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新白名单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWhitelist(Long id) {
        try {
            int result = whitelistMapper.deleteWhitelistById(id);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除白名单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWhitelistBatch(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }

            int result = whitelistMapper.deleteWhitelistByIds(ids);
            if (result > 0) {
                // 刷新缓存
                refreshCache();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("批量删除白名单失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importWhitelist(List<String> words, Long creatorId) {
        int total = words.size();
        int success = 0;
        int duplicate = 0;
        List<String> errors = new ArrayList<>();

        try {
            List<SensitiveWhitelist> validWhitelists = new ArrayList<>();

            for (String word : words) {
                if (StrUtil.isBlank(word)) {
                    continue;
                }

                word = word.trim();

                // 检查是否已存在
                Integer exists = whitelistMapper.existsInWhitelist(word, "global", null);
                if (exists != null && exists > 0) {
                    duplicate++;
                    continue;
                }

                // 创建白名单对象
                SensitiveWhitelist whitelist = new SensitiveWhitelist();
                whitelist.setWord(word);
                whitelist.setScope("global");
                whitelist.setStatus(1);
                whitelist.setCreatorId(creatorId);

                validWhitelists.add(whitelist);
            }

            // 批量插入
            if (!validWhitelists.isEmpty()) {
                int result = whitelistMapper.batchInsertWhitelist(validWhitelists);
                success = result;
            }

            // 刷新缓存
            if (success > 0) {
                refreshCache();
            }

        } catch (Exception e) {
            log.error("批量导入白名单失败", e);
            errors.add("导入过程中发生异常：" + e.getMessage());
        }

        return new ImportResult(total, success, duplicate, errors);
    }

    @Override
    public void refreshCache() {
        try {
            // 清空缓存
            cache.invalidateAll();
            
            // 预加载所有启用的白名单词汇
            List<String> words = whitelistMapper.selectEnabledWords();
            for (String word : words) {
                if (StrUtil.isNotBlank(word)) {
                    String cacheKey = buildCacheKey(word, null);
                    cache.put(cacheKey, true);
                }
            }
            
            log.info("白名单缓存刷新完成，加载词汇数量: {}", words.size());
        } catch (Exception e) {
            log.error("刷新白名单缓存失败", e);
        }
    }

    /**
     * 构建缓存Key
     */
    private String buildCacheKey(String word, String module) {
        if (StrUtil.isBlank(module)) {
            return "whitelist:global:" + word;
        }
        return "whitelist:module:" + module + ":" + word;
    }
}
