package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveWhitelist;
import com.xiaou.sensitive.dto.SensitiveWhitelistQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感词白名单Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveWhitelistMapper {

    /**
     * 分页查询白名单列表
     *
     * @param query 查询条件
     * @return 白名单列表
     */
    List<SensitiveWhitelist> selectWhitelistList(SensitiveWhitelistQuery query);

    /**
     * 根据ID查询白名单
     *
     * @param id 白名单ID
     * @return 白名单信息
     */
    SensitiveWhitelist selectWhitelistById(Long id);

    /**
     * 检查白名单是否存在
     *
     * @param word 词汇
     * @param scope 作用范围
     * @param moduleName 模块名称
     * @return 是否存在
     */
    Integer existsInWhitelist(@Param("word") String word, 
                             @Param("scope") String scope, 
                             @Param("moduleName") String moduleName);

    /**
     * 查询所有启用的白名单词汇
     *
     * @return 白名单词汇列表
     */
    List<String> selectEnabledWords();

    /**
     * 新增白名单
     *
     * @param whitelist 白名单信息
     * @return 影响行数
     */
    int insertWhitelist(SensitiveWhitelist whitelist);

    /**
     * 批量新增白名单
     *
     * @param list 白名单列表
     * @return 影响行数
     */
    int batchInsertWhitelist(List<SensitiveWhitelist> list);

    /**
     * 更新白名单
     *
     * @param whitelist 白名单信息
     * @return 影响行数
     */
    int updateWhitelist(SensitiveWhitelist whitelist);

    /**
     * 删除白名单
     *
     * @param id 白名单ID
     * @return 影响行数
     */
    int deleteWhitelistById(Long id);

    /**
     * 批量删除白名单
     *
     * @param ids 白名单ID列表
     * @return 影响行数
     */
    int deleteWhitelistByIds(List<Long> ids);
}
