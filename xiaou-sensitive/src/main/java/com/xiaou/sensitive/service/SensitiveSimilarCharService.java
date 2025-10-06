package com.xiaou.sensitive.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.sensitive.domain.SensitiveSimilarChar;
import com.xiaou.sensitive.dto.SensitiveSimilarCharQuery;

import java.util.List;

/**
 * 形似字映射服务接口
 *
 * @author xiaou
 */
public interface SensitiveSimilarCharService {

    /**
     * 分页查询形似字列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SensitiveSimilarChar> listSimilarChars(SensitiveSimilarCharQuery query);

    /**
     * 根据ID查询形似字
     *
     * @param id 形似字ID
     * @return 形似字信息
     */
    SensitiveSimilarChar getSimilarCharById(Integer id);

    /**
     * 新增形似字
     *
     * @param similarChar 形似字信息
     * @return 是否成功
     */
    boolean addSimilarChar(SensitiveSimilarChar similarChar);

    /**
     * 批量新增形似字
     *
     * @param similarChars 形似字列表
     * @return 是否成功
     */
    boolean batchAddSimilarChars(List<SensitiveSimilarChar> similarChars);

    /**
     * 更新形似字
     *
     * @param similarChar 形似字信息
     * @return 是否成功
     */
    boolean updateSimilarChar(SensitiveSimilarChar similarChar);

    /**
     * 删除形似字
     *
     * @param id 形似字ID
     * @return 是否成功
     */
    boolean deleteSimilarChar(Integer id);

    /**
     * 刷新形似字缓存
     */
    void refreshCache();
}
