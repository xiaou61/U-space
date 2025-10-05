package com.xiaou.sensitive.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.domain.SensitiveSource;
import com.xiaou.sensitive.dto.SensitiveSourceQuery;
import com.xiaou.sensitive.mapper.SensitiveSourceMapper;
import com.xiaou.sensitive.service.SensitiveSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 敏感词来源管理服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveSourceServiceImpl implements SensitiveSourceService {

    private final SensitiveSourceMapper sourceMapper;

    @Override
    public PageResult<SensitiveSource> listSources(SensitiveSourceQuery query) {
        return PageHelper.doPage(query.getPageNum(), query.getPageSize(), () -> {
            return sourceMapper.selectSourceList(query);
        });
    }

    @Override
    public SensitiveSource getSourceById(Integer id) {
        return sourceMapper.selectSourceById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSource(SensitiveSource source) {
        try {
            // 设置默认值
            if (source.getStatus() == null) {
                source.setStatus(1);
            }
            if (source.getSyncInterval() == null) {
                source.setSyncInterval(24); // 默认24小时
            }

            int result = sourceMapper.insertSource(source);
            if (result > 0) {
                log.info("新增词库来源成功: sourceName={}", source.getSourceName());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("新增词库来源失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSource(SensitiveSource source) {
        try {
            int result = sourceMapper.updateSource(source);
            if (result > 0) {
                log.info("更新词库来源成功: id={}", source.getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新词库来源失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSource(Integer id) {
        try {
            int result = sourceMapper.deleteSourceById(id);
            if (result > 0) {
                log.info("删除词库来源成功: id={}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除词库来源失败", e);
            return false;
        }
    }

    @Override
    public String testConnection(Integer id) {
        try {
            SensitiveSource source = sourceMapper.selectSourceById(id);
            if (source == null) {
                return "词库来源不存在";
            }

            if (StrUtil.isBlank(source.getApiUrl())) {
                return "API地址为空，无需测试连接";
            }

            // 测试HTTP连接
            HttpResponse response = HttpRequest.get(source.getApiUrl())
                    .timeout(5000)
                    .execute();

            if (response.isOk()) {
                return "连接成功";
            } else {
                return "连接失败: HTTP " + response.getStatus();
            }
        } catch (Exception e) {
            log.error("测试连接失败: id={}", id, e);
            return "连接失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResult syncSource(Integer id) {
        try {
            SensitiveSource source = sourceMapper.selectSourceById(id);
            if (source == null) {
                return new SyncResult(false, 0, 0, 0, "词库来源不存在");
            }

            if (source.getStatus() == 0) {
                return new SyncResult(false, 0, 0, 0, "词库来源已禁用");
            }

            // 根据来源类型执行不同的同步逻辑
            switch (source.getSourceType()) {
                case "local":
                    return syncLocalSource(source);
                case "api":
                    return syncApiSource(source);
                case "github":
                    return syncGithubSource(source);
                default:
                    return new SyncResult(false, 0, 0, 0, "不支持的来源类型");
            }
        } catch (Exception e) {
            log.error("同步词库失败: id={}", id, e);
            return new SyncResult(false, 0, 0, 0, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步本地来源
     */
    private SyncResult syncLocalSource(SensitiveSource source) {
        // 本地来源无需同步
        updateSyncStatus(source.getId(), 1, 0);
        return new SyncResult(true, 0, 0, 0, "本地来源无需同步");
    }

    /**
     * 同步API来源
     */
    private SyncResult syncApiSource(SensitiveSource source) {
        try {
            if (StrUtil.isBlank(source.getApiUrl())) {
                return new SyncResult(false, 0, 0, 0, "API地址为空");
            }

            // TODO: 实现API同步逻辑
            // 1. 调用API获取词库数据
            // 2. 解析数据
            // 3. 批量导入敏感词表
            // 4. 更新同步状态

            updateSyncStatus(source.getId(), 1, 0);
            return new SyncResult(true, 0, 0, 0, "API同步功能待实现");
        } catch (Exception e) {
            updateSyncStatus(source.getId(), 0, 0);
            log.error("同步API来源失败", e);
            return new SyncResult(false, 0, 0, 0, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步GitHub来源
     */
    private SyncResult syncGithubSource(SensitiveSource source) {
        try {
            if (StrUtil.isBlank(source.getApiUrl())) {
                return new SyncResult(false, 0, 0, 0, "GitHub地址为空");
            }

            // TODO: 实现GitHub同步逻辑
            // 1. 获取GitHub文件内容
            // 2. 解析数据
            // 3. 批量导入敏感词表
            // 4. 更新同步状态

            updateSyncStatus(source.getId(), 1, 0);
            return new SyncResult(true, 0, 0, 0, "GitHub同步功能待实现");
        } catch (Exception e) {
            updateSyncStatus(source.getId(), 0, 0);
            log.error("同步GitHub来源失败", e);
            return new SyncResult(false, 0, 0, 0, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 更新同步状态
     */
    private void updateSyncStatus(Integer sourceId, Integer syncStatus, Integer wordCount) {
        SensitiveSource source = new SensitiveSource();
        source.setId(sourceId);
        source.setSyncStatus(syncStatus);
        source.setLastSyncTime(LocalDateTime.now());
        if (wordCount > 0) {
            source.setWordCount(wordCount);
        }
        sourceMapper.updateSource(source);
    }
}
