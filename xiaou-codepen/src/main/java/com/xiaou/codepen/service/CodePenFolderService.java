package com.xiaou.codepen.service;

import com.xiaou.codepen.domain.CodePenFolder;

import java.util.List;

/**
 * 收藏夹Service
 * 
 * @author xiaou
 */
public interface CodePenFolderService {
    
    /**
     * 创建收藏夹
     */
    Long createFolder(String folderName, String folderDescription, Long userId);
    
    /**
     * 更新收藏夹
     */
    boolean updateFolder(Long id, String folderName, String folderDescription, Long userId);
    
    /**
     * 删除收藏夹
     */
    boolean deleteFolder(Long id, Long userId);
    
    /**
     * 获取用户的收藏夹列表
     */
    List<CodePenFolder> getFolderList(Long userId);
    
    /**
     * 获取收藏夹内容列表
     */
    List<Long> getFolderItems(Long folderId, Long userId);
}

