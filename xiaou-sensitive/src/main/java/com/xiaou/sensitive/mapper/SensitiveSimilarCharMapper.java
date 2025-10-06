package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveSimilarChar;
import com.xiaou.sensitive.dto.SensitiveSimilarCharQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 形似字映射Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveSimilarCharMapper {

    /**
     * 分页查询形似字列表
     *
     * @param query 查询条件
     * @return 形似字列表
     */
    List<SensitiveSimilarChar> selectSimilarCharList(SensitiveSimilarCharQuery query);

    /**
     * 根据ID查询形似字
     *
     * @param id 形似字ID
     * @return 形似字信息
     */
    SensitiveSimilarChar selectSimilarCharById(Integer id);

    /**
     * 查询所有启用的形似字映射
     *
     * @return 形似字映射列表
     */
    List<SensitiveSimilarChar> selectEnabledSimilarChars();

    /**
     * 新增形似字
     *
     * @param similarChar 形似字信息
     * @return 影响行数
     */
    int insertSimilarChar(SensitiveSimilarChar similarChar);

    /**
     * 批量新增形似字
     *
     * @param list 形似字列表
     * @return 影响行数
     */
    int batchInsertSimilarChar(List<SensitiveSimilarChar> list);

    /**
     * 更新形似字
     *
     * @param similarChar 形似字信息
     * @return 影响行数
     */
    int updateSimilarChar(SensitiveSimilarChar similarChar);

    /**
     * 删除形似字
     *
     * @param id 形似字ID
     * @return 影响行数
     */
    int deleteSimilarCharById(Integer id);
}
