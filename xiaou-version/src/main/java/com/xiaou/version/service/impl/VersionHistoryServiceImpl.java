package com.xiaou.version.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.version.domain.VersionHistory;
import com.xiaou.version.dto.VersionHistoryCreateRequest;
import com.xiaou.version.dto.VersionHistoryQueryRequest;
import com.xiaou.version.dto.VersionHistoryResponse;
import com.xiaou.version.dto.VersionHistoryUpdateRequest;
import com.xiaou.version.mapper.VersionHistoryMapper;
import com.xiaou.version.service.VersionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 版本历史服务实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VersionHistoryServiceImpl implements VersionHistoryService {
    
    private final VersionHistoryMapper versionHistoryMapper;

    private final UserContextUtil userContextUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(VersionHistoryCreateRequest request) {
        // 检查版本号是否已存在
        if (checkVersionNumberExists(request.getVersionNumber(), null)) {
            throw new BusinessException("版本号已存在");
        }
        
        // 转换为实体类
        VersionHistory versionHistory = new VersionHistory();
        BeanUtil.copyProperties(request, versionHistory);
        
        // 设置发布时间
        try {
            versionHistory.setReleaseTime(DateUtil.parse(request.getReleaseTime(), "yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new BusinessException("发布时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }
        
        // 设置创建信息
        versionHistory.setCreatedBy(userContextUtil.getCurrentUserId());
        versionHistory.setViewCount(0);
        
        // 插入数据库
        int result = versionHistoryMapper.insert(versionHistory);
        if (result <= 0) {
            throw new BusinessException("创建版本历史记录失败");
        }
        
        log.info("用户[{}]创建版本历史记录成功，版本号: {}", userContextUtil.getCurrentUserId(), request.getVersionNumber());
        return versionHistory.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVersion(VersionHistoryUpdateRequest request) {
        // 检查记录是否存在
        VersionHistory existingVersion = versionHistoryMapper.selectById(request.getId());
        if (existingVersion == null) {
            throw new BusinessException("版本历史记录不存在");
        }
        
        // 检查版本号是否已存在（排除自己）
        if (checkVersionNumberExists(request.getVersionNumber(), request.getId())) {
            throw new BusinessException("版本号已存在");
        }
        
        // 转换为实体类
        VersionHistory versionHistory = new VersionHistory();
        BeanUtil.copyProperties(request, versionHistory);
        
        // 设置发布时间
        try {
            versionHistory.setReleaseTime(DateUtil.parse(request.getReleaseTime(), "yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new BusinessException("发布时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }
        
        // 设置更新信息
        versionHistory.setUpdatedBy(userContextUtil.getCurrentUserId());
        
        // 更新数据库
        int result = versionHistoryMapper.updateById(versionHistory);
        if (result <= 0) {
            throw new BusinessException("更新版本历史记录失败");
        }
        
        log.info("用户[{}]更新版本历史记录成功，ID: {}, 版本号: {}", userContextUtil.getCurrentUserId(), request.getId(), request.getVersionNumber());
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVersion(Long id) {
        // 检查记录是否存在
        VersionHistory existingVersion = versionHistoryMapper.selectById(id);
        if (existingVersion == null) {
            throw new BusinessException("版本历史记录不存在");
        }
        
        // 逻辑删除
        int result = versionHistoryMapper.logicalDeleteById(id, userContextUtil.getCurrentUserId());
        if (result <= 0) {
            throw new BusinessException("删除版本历史记录失败");
        }
        
        log.info("用户[{}]删除版本历史记录成功，ID: {}, 版本号: {}", userContextUtil.getCurrentUserId(), id, existingVersion.getVersionNumber());
        return true;
    }
    
    @Override
    public VersionHistoryResponse getVersionDetail(Long id) {
        VersionHistory versionHistory = versionHistoryMapper.selectById(id);
        if (versionHistory == null) {
            throw new BusinessException("版本历史记录不存在");
        }
        
        return convertToResponse(versionHistory);
    }
    
    @Override
    public PageResult<VersionHistoryResponse> getAdminVersionList(VersionHistoryQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            List<VersionHistory> versionHistories = versionHistoryMapper.selectByPage(request);
            return versionHistories.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public PageResult<VersionHistoryResponse> getPublishedVersionList(VersionHistoryQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            List<VersionHistory> versionHistories = versionHistoryMapper.selectPublishedByPage(request);
            return versionHistories.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementViewCount(Long id) {
        int result = versionHistoryMapper.incrementViewCount(id);
        return result > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishVersion(Long id) {
        int result = versionHistoryMapper.updateStatus(id, 1, userContextUtil.getCurrentUserId());
        if (result > 0) {
            log.info("用户[{}]发布版本历史记录成功，ID: {}", userContextUtil.getCurrentUserId(), id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean hideVersion(Long id) {
        int result = versionHistoryMapper.updateStatus(id, 2, userContextUtil.getCurrentUserId());
        if (result > 0) {
            log.info("用户[{}]隐藏版本历史记录成功，ID: {}", userContextUtil.getCurrentUserId(), id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unpublishVersion(Long id) {
        int result = versionHistoryMapper.updateStatus(id, 0, userContextUtil.getCurrentUserId());
        if (result > 0) {
            log.info("用户[{}]取消发布版本历史记录成功，ID: {}", userContextUtil.getCurrentUserId(), id);
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPublishVersions(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        int result = versionHistoryMapper.batchUpdateStatus(ids, 1, userContextUtil.getCurrentUserId());
        if (result > 0) {
            log.info("用户[{}]批量发布版本历史记录成功，数量: {}", userContextUtil.getCurrentUserId(), ids.size());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchHideVersions(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        int result = versionHistoryMapper.batchUpdateStatus(ids, 2, userContextUtil.getCurrentUserId());
        if (result > 0) {
            log.info("用户[{}]批量隐藏版本历史记录成功，数量: {}", userContextUtil.getCurrentUserId(), ids.size());
            return true;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteVersions(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        int result = versionHistoryMapper.batchLogicalDelete(ids, userContextUtil.getCurrentUserId());
        if (result > 0) {
            log.info("用户[{}]批量删除版本历史记录成功，数量: {}", userContextUtil.getCurrentUserId(), ids.size());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean checkVersionNumberExists(String versionNumber, Long excludeId) {
        if (StrUtil.isBlank(versionNumber)) {
            return false;
        }
        
        VersionHistory existingVersion = versionHistoryMapper.selectByVersionNumber(versionNumber, 0);
        if (existingVersion == null) {
            return false;
        }
        
        // 如果有排除ID，且查到的记录ID等于排除ID，则认为不存在冲突
        return excludeId == null || !Objects.equals(existingVersion.getId(), excludeId);
    }
    
    @Override
    public List<VersionHistoryResponse> getLatestVersions(int limit) {
        VersionHistoryQueryRequest request = new VersionHistoryQueryRequest();
        request.setPageNum(1);
        request.setPageSize(limit);
        request.setStatus(1); // 只查询已发布的
        
        List<VersionHistory> versionHistories = versionHistoryMapper.selectPublishedByPage(request);
        return versionHistories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应DTO
     */
    private VersionHistoryResponse convertToResponse(VersionHistory versionHistory) {
        VersionHistoryResponse response = new VersionHistoryResponse();
        BeanUtil.copyProperties(versionHistory, response);
        
        // 设置类型名称
        response.setUpdateTypeName(getUpdateTypeName(versionHistory.getUpdateType()));
        
        // 设置状态名称
        response.setStatusName(getStatusName(versionHistory.getStatus()));
        
        return response;
    }
    
    /**
     * 获取更新类型名称
     */
    private String getUpdateTypeName(Integer updateType) {
        if (updateType == null) {
            return "未知";
        }
        
        switch (updateType) {
            case 1:
                return "重大更新";
            case 2:
                return "功能更新";
            case 3:
                return "修复更新";
            case 4:
                return "其他";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "已发布";
            case 2:
                return "已隐藏";
            default:
                return "未知";
        }
    }
} 