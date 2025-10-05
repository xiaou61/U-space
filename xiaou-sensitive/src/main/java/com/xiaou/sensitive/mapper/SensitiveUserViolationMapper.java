package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveUserViolation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户违规统计Mapper
 *
 * @author xiaou
 */
@Mapper
public interface SensitiveUserViolationMapper {

    /**
     * 根据用户和日期查询违规记录
     *
     * @param userId 用户ID
     * @param statDate 统计日期
     * @return 违规记录
     */
    SensitiveUserViolation selectByUserAndDate(@Param("userId") Long userId, 
                                               @Param("statDate") LocalDate statDate);

    /**
     * 新增违规记录
     *
     * @param violation 违规信息
     * @return 影响行数
     */
    int insert(SensitiveUserViolation violation);

    /**
     * 更新违规记录
     *
     * @param violation 违规信息
     * @return 影响行数
     */
    int updateById(SensitiveUserViolation violation);

    /**
     * 查询违规用户列表（按违规次数排序）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 违规用户列表
     */
    List<SensitiveUserViolation> selectViolationUsers(@Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate, 
                                                      @Param("limit") Integer limit);

    /**
     * 查询被限制的用户数
     *
     * @return 被限制用户数
     */
    Long countRestrictedUsers();
}
