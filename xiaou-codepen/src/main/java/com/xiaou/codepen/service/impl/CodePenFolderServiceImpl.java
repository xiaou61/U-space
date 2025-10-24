package com.xiaou.codepen.service.impl;

import com.xiaou.codepen.domain.CodePenFolder;
import com.xiaou.codepen.mapper.CodePenCollectMapper;
import com.xiaou.codepen.mapper.CodePenFolderMapper;
import com.xiaou.codepen.service.CodePenFolderService;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收藏夹Service实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodePenFolderServiceImpl implements CodePenFolderService {
    
    private final CodePenFolderMapper folderMapper;
    private final CodePenCollectMapper collectMapper;
    
    @Override
    public Long createFolder(String folderName, String folderDescription, Long userId) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new BusinessException("收藏夹名称不能为空");
        }
        
        CodePenFolder folder = new CodePenFolder();
        folder.setUserId(userId);
        folder.setFolderName(folderName);
        folder.setFolderDescription(folderDescription);
        folder.setCollectCount(0);
        
        folderMapper.insert(folder);
        
        return folder.getId();
    }
    
    @Override
    public boolean updateFolder(Long id, String folderName, String folderDescription, Long userId) {
        CodePenFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        CodePenFolder updateFolder = new CodePenFolder();
        updateFolder.setId(id);
        updateFolder.setFolderName(folderName);
        updateFolder.setFolderDescription(folderDescription);
        
        return folderMapper.updateById(updateFolder) > 0;
    }
    
    @Override
    public boolean deleteFolder(Long id, Long userId) {
        CodePenFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        return folderMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<CodePenFolder> getFolderList(Long userId) {
        return folderMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Long> getFolderItems(Long folderId, Long userId) {
        return collectMapper.selectPenIdsByFolderId(folderId, userId);
    }
}

