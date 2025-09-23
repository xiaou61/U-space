package com.xiaou.moyu.mapper;

import com.xiaou.moyu.domain.WorkRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 工作记录数据访问层
 *
 * @author xiaou
 */
@Mapper
public interface WorkRecordMapper {

    /**
     * 根据用户ID和日期查询工作记录
     *
     * @param userId 用户ID
     * @param workDate 工作日期
     * @return 工作记录
     */
    WorkRecord selectByUserIdAndDate(@Param("userId") Long userId, @Param("workDate") LocalDate workDate);

    /**
     * 查询用户指定日期范围的工作记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作记录列表
     */
    List<WorkRecord> selectByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 插入工作记录
     *
     * @param record 工作记录
     * @return 影响行数
     */
    int insert(WorkRecord record);

    /**
     * 更新工作记录
     *
     * @param record 工作记录
     * @return 影响行数
     */
    int update(WorkRecord record);

    /**
     * 插入或更新工作记录
     *
     * @param record 工作记录
     * @return 影响行数
     */
    int insertOrUpdate(WorkRecord record);

    /**
     * 根据用户ID和日期删除工作记录
     *
     * @param userId 用户ID
     * @param workDate 工作日期
     * @return 影响行数
     */
    int deleteByUserIdAndDate(@Param("userId") Long userId, @Param("workDate") LocalDate workDate);
}
