package com.xiaou.moyu.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.moyu.domain.BugItem;
import com.xiaou.moyu.dto.AdminBugItemRequest;
import com.xiaou.moyu.dto.BugItemDto;
import com.xiaou.moyu.dto.BugItemQueryRequest;

import java.util.List;

/**
 * Bug商店服务接口
 *
 * @author xiaou
 */
public interface BugStoreService {
    
    /**
     * 随机获取一个Bug（用户端）
     */
    BugItemDto getRandomBug(Long userId);
    
    /**
     * 分页查询Bug列表（管理端）
     */
    PageResult<BugItem> getBugList(BugItemQueryRequest query);
    
    /**
     * 根据ID获取Bug详情（管理端）
     */
    BugItem getBugById(Long id);
    
    /**
     * 添加Bug（管理端）
     */
    Long addBug(AdminBugItemRequest request, Long createdBy);
    
    /**
     * 更新Bug（管理端）
     */
    boolean updateBug(AdminBugItemRequest request);
    
    /**
     * 删除Bug（管理端）
     */
    boolean deleteBug(Long id);
    
    /**
     * 批量导入Bug
     */
    boolean batchImportBugs(List<AdminBugItemRequest> requests, Long createdBy);
    
    /**
     * 记录用户浏览历史
     */
    void recordUserView(Long userId, Long bugId);
}
