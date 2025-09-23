package com.xiaou.moyu.mapper;

import com.xiaou.moyu.domain.BugItem;
import com.xiaou.moyu.dto.BugItemQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Bug条目Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface BugItemMapper {
    
    /**
     * 插入Bug条目
     */
    int insert(BugItem bugItem);
    
    /**
     * 根据ID删除Bug条目
     */
    int deleteById(Long id);
    
    /**
     * 更新Bug条目
     */
    int updateById(BugItem bugItem);
    
    /**
     * 根据ID查询Bug条目
     */
    BugItem selectById(Long id);
    
    /**
     * 分页查询Bug条目
     */
    List<BugItem> selectByPage(@Param("query") BugItemQueryRequest query);
    
    /**
     * 查询Bug条目总数
     */
    Long countByCondition(@Param("query") BugItemQueryRequest query);
    
    /**
     * 随机获取一个启用的Bug条目（排除用户最近浏览的）
     */
    BugItem selectRandomBug(@Param("userId") Long userId, @Param("excludeIds") List<Long> excludeIds, @Param("limit") Integer limit);
    
    /**
     * 查询所有启用的Bug条目ID
     */
    List<Long> selectEnabledBugIds();
    
    /**
     * 批量插入Bug条目
     */
    int insertBatch(@Param("list") List<BugItem> bugItems);
}
