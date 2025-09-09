package com.xiaou.filestorage.mapper;

import com.xiaou.filestorage.domain.FileAccess;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件访问记录数据访问接口
 *
 * @author xiaou
 */
@Mapper
public interface FileAccessMapper {

    /**
     * 插入文件访问记录
     *
     * @param fileAccess 访问记录
     * @return 影响行数
     */
    int insert(FileAccess fileAccess);

    /**
     * 根据ID查询访问记录
     *
     * @param id 记录ID
     * @return 访问记录
     */
    FileAccess selectById(@Param("id") Long id);

    /**
     * 查询文件的访问记录
     *
     * @param fileId 文件ID
     * @param limit  限制数量
     * @return 访问记录列表
     */
    List<FileAccess> selectByFileId(@Param("fileId") Long fileId, @Param("limit") Integer limit);

    /**
     * 按条件查询访问记录
     *
     * @param fileId     文件ID
     * @param moduleName 模块名称
     * @param accessType 访问类型
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 访问记录列表
     */
    List<FileAccess> selectByCondition(@Param("fileId") Long fileId,
                                       @Param("moduleName") String moduleName,
                                       @Param("accessType") String accessType,
                                       @Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime);

    /**
     * 统计文件访问次数
     *
     * @param fileId    文件ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 访问次数
     */
    Long countByFileId(@Param("fileId") Long fileId,
                       @Param("startTime") Date startTime,
                       @Param("endTime") Date endTime);

    /**
     * 统计模块访问次数
     *
     * @param moduleName 模块名称
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 访问次数
     */
    Long countByModule(@Param("moduleName") String moduleName,
                       @Param("startTime") Date startTime,
                       @Param("endTime") Date endTime);

    /**
     * 删除指定时间之前的访问记录
     *
     * @param beforeTime 时间界限
     * @return 删除数量
     */
    int deleteBeforeTime(@Param("beforeTime") Date beforeTime);

    /**
     * 查询热门文件
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 文件访问统计列表
     */
    @MapKey("fileId")
    List<Map<String, Object>> selectHotFiles(@Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime,
                                            @Param("limit") Integer limit);
} 