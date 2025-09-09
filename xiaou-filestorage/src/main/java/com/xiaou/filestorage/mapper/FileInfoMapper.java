package com.xiaou.filestorage.mapper;

import com.xiaou.filestorage.domain.FileInfo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件信息数据访问接口
 *
 * @author xiaou
 */
@Mapper
public interface FileInfoMapper {

    /**
     * 插入文件信息
     *
     * @param fileInfo 文件信息
     * @return 影响行数
     */
    int insert(FileInfo fileInfo);

    /**
     * 根据ID查询文件信息
     *
     * @param id 文件ID
     * @return 文件信息
     */
    FileInfo selectById(@Param("id") Long id);

    /**
     * 根据MD5查询文件信息
     *
     * @param md5Hash MD5值
     * @return 文件信息
     */
    FileInfo selectByMd5(@Param("md5Hash") String md5Hash);

    /**
     * 根据ID列表查询文件信息
     *
     * @param ids ID列表
     * @return 文件信息列表
     */
    List<FileInfo> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 分页查询文件信息
     *
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @param status       状态
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 文件信息列表
     */
    List<FileInfo> selectByCondition(@Param("moduleName") String moduleName,
                                     @Param("businessType") String businessType,
                                     @Param("status") Integer status,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime);

    /**
     * 更新文件信息
     *
     * @param fileInfo 文件信息
     * @return 影响行数
     */
    int updateById(FileInfo fileInfo);

    /**
     * 逻辑删除文件
     *
     * @param id 文件ID
     * @return 影响行数
     */
    int logicalDeleteById(@Param("id") Long id);

    /**
     * 物理删除文件
     *
     * @param id 文件ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计文件数量
     *
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @param status       状态
     * @return 文件数量
     */
    Long countByCondition(@Param("moduleName") String moduleName,
                          @Param("businessType") String businessType,
                          @Param("status") Integer status);

    /**
     * 统计存储容量
     *
     * @param moduleName   模块名称
     * @param businessType 业务类型
     * @param status       状态
     * @return 存储容量(字节)
     */
    Long sumFileSizeByCondition(@Param("moduleName") String moduleName,
                                @Param("businessType") String businessType,
                                @Param("status") Integer status);

    /**
     * 查询需要迁移的文件
     *
     * @param sourceStorageId 源存储配置ID
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param limit           限制数量
     * @return 文件信息列表
     */
    List<FileInfo> selectForMigration(@Param("sourceStorageId") Long sourceStorageId,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime,
                                      @Param("limit") Integer limit);

    /**
     * 按时间范围统计文件数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 文件数量
     */
    Long countByTimeRange(@Param("startTime") Date startTime,
                          @Param("endTime") Date endTime);

    /**
     * 按模块统计文件使用情况
     *
     * @return 模块使用统计列表
     */
    @MapKey("moduleName")
    List<Map<String, Object>> countByModule();

    /**
     * 按存储类型统计文件使用情况
     *
     * @return 存储类型使用统计列表
     */
    @MapKey("storageType")
    List<Map<String, Object>> countByStorageType();

    /**
     * 按文件类型统计
     *
     * @return 文件类型统计列表
     */
    @MapKey("fileType")
    List<Map<String, Object>> countByFileType();

    /**
     * 按日期统计上传趋势
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 上传趋势数据
     */
    @MapKey("uploadDate")
    List<Map<String, Object>> countUploadTrend(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);
} 