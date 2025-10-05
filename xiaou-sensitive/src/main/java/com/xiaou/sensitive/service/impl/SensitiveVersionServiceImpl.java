package com.xiaou.sensitive.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.sensitive.domain.SensitiveVersion;
import com.xiaou.sensitive.mapper.SensitiveVersionMapper;
import com.xiaou.sensitive.service.SensitiveVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 敏感词版本管理服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveVersionServiceImpl implements SensitiveVersionService {

    private final SensitiveVersionMapper versionMapper;

    @Override
    public PageResult<SensitiveVersion> listVersions(Integer pageNum, Integer pageSize) {
        return PageHelper.doPage(pageNum, pageSize, versionMapper::selectVersionList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createVersion(String changeType, Integer changeCount, String changeDetail,
                                Long operatorId, String remark) {
        try {
            // 生成版本号
            String versionNo = generateVersionNo();

            SensitiveVersion version = new SensitiveVersion();
            version.setVersionNo(versionNo);
            version.setChangeType(changeType);
            version.setChangeCount(changeCount);
            version.setChangeDetail(changeDetail);
            version.setOperatorId(operatorId);
            version.setRemark(remark);

            int result = versionMapper.insertVersion(version);
            if (result > 0) {
                log.info("创建版本记录成功: versionNo={}, changeType={}", versionNo, changeType);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("创建版本记录失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackToVersion(Long versionId, Long operatorId) {
        try {
            SensitiveVersion version = versionMapper.selectVersionById(versionId);
            if (version == null) {
                log.warn("版本不存在: versionId={}", versionId);
                return false;
            }

            // 解析变更详情
            if (StrUtil.isBlank(version.getChangeDetail())) {
                log.warn("版本变更详情为空，无法回滚");
                return false;
            }

            // 创建回滚版本记录
            String rollbackDetail = JSONUtil.toJsonStr(Map.of(
                "rollback_from", version.getVersionNo(),
                "rollback_time", LocalDateTime.now().toString()
            ));

            createVersion("rollback", 0, rollbackDetail, operatorId, 
                "回滚到版本: " + version.getVersionNo());

            log.info("回滚版本成功: targetVersion={}", version.getVersionNo());
            return true;
        } catch (Exception e) {
            log.error("回滚版本失败: versionId={}", versionId, e);
            return false;
        }
    }

    @Override
    public SensitiveVersion getVersionById(Long versionId) {
        return versionMapper.selectVersionById(versionId);
    }

    @Override
    public String getLatestVersionNo() {
        SensitiveVersion latestVersion = versionMapper.selectLatestVersion();
        return latestVersion != null ? latestVersion.getVersionNo() : "v1.0.0";
    }

    /**
     * 生成版本号（格式：v2.0.yyyyMMddHHmm）
     */
    private String generateVersionNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return "v2.0." + timestamp;
    }
}
