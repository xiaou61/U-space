package com.xiaou.sensitive.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sensitive.domain.SensitiveWhitelist;
import com.xiaou.sensitive.dto.SensitiveWhitelistQuery;

import java.util.List;

/**
 * 敏感词白名单服务接口
 *
 * @author xiaou
 */
public interface SensitiveWhitelistService {

    /**
     * 分页查询白名单列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SensitiveWhitelist> listWhitelist(SensitiveWhitelistQuery query);

    /**
     * 根据ID查询白名单
     *
     * @param id 白名单ID
     * @return 白名单信息
     */
    SensitiveWhitelist getWhitelistById(Long id);

    /**
     * 检查词汇是否在白名单中
     *
     * @param word 词汇
     * @param module 模块名称（可选）
     * @return 是否在白名单中
     */
    boolean isInWhitelist(String word, String module);

    /**
     * 新增白名单
     *
     * @param whitelist 白名单信息
     * @return 是否成功
     */
    boolean addWhitelist(SensitiveWhitelist whitelist);

    /**
     * 更新白名单
     *
     * @param whitelist 白名单信息
     * @return 是否成功
     */
    boolean updateWhitelist(SensitiveWhitelist whitelist);

    /**
     * 删除白名单
     *
     * @param id 白名单ID
     * @return 是否成功
     */
    boolean deleteWhitelist(Long id);

    /**
     * 批量删除白名单
     *
     * @param ids 白名单ID列表
     * @return 是否成功
     */
    boolean deleteWhitelistBatch(List<Long> ids);

    /**
     * 批量导入白名单
     *
     * @param words 词汇列表
     * @param creatorId 创建人ID
     * @return 导入结果
     */
    ImportResult importWhitelist(List<String> words, Long creatorId);

    /**
     * 刷新白名单缓存
     */
    void refreshCache();

    /**
     * 导入结果
     */
    class ImportResult {
        private int total;
        private int success;
        private int duplicate;
        private List<String> errors;

        public ImportResult(int total, int success, int duplicate, List<String> errors) {
            this.total = total;
            this.success = success;
            this.duplicate = duplicate;
            this.errors = errors;
        }

        public int getTotal() { return total; }
        public int getSuccess() { return success; }
        public int getDuplicate() { return duplicate; }
        public List<String> getErrors() { return errors; }
    }
}
