package com.xiaou.fileupload.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 文件记录表
 * </p>
 */
@Data
@Accessors(chain = true)
@TableName("u_file_detail")
public class FileDetail {


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("url")
    private String url;


    @TableField("size")
    private Long size;

    @TableField("filename")
    private String filename;

    @TableField("original_filename")
    private String originalFilename;

    @TableField("base_path")
    private String basePath;

    @TableField("path")
    private String path;

    @TableField("ext")
    private String ext;

    @TableField("content_type")
    private String contentType;

    @TableField("platform")
    private String platform;


    @TableField("th_url")
    private String thUrl;


    @TableField("th_filename")
    private String thFilename;


    @TableField("th_size")
    private Long thSize;


    @TableField("th_content_type")
    private String thContentType;


    @TableField("object_id")
    private String objectId;


    @TableField("object_type")
    private String objectType;


    @TableField("metadata")
    private String metadata;


    @TableField("user_metadata")
    private String userMetadata;


    @TableField("th_metadata")
    private String thMetadata;


    @TableField("th_user_metadata")
    private String thUserMetadata;

    @TableField("attr")
    private String attr;


    @TableField("file_acl")
    private String fileAcl;


    @TableField("th_file_acl")
    private String thFileAcl;


    @TableField("hash_info")
    private String hashInfo;


    @TableField("upload_id")
    private String uploadId;

    @TableField("upload_status")
    private Integer uploadStatus;


    @TableField("create_time")
    private LocalDateTime createTime;


    public static final String COL_ID = "id";

    public static final String COL_URL = "url";

    public static final String COL_SIZE = "size";

    public static final String COL_FILENAME = "filename";

    public static final String COL_ORIGINAL_FILENAME = "original_filename";

    public static final String COL_BASE_PATH = "base_path";

    public static final String COL_PATH = "path";

    public static final String COL_EXT = "ext";

    public static final String COL_CONTENT_TYPE = "content_type";

    public static final String COL_PLATFORM = "platform";

    public static final String COL_TH_URL = "th_url";

    public static final String COL_TH_FILENAME = "th_filename";

    public static final String COL_TH_SIZE = "th_size";

    public static final String COL_TH_CONTENT_TYPE = "th_content_type";

    public static final String COL_OBJECT_ID = "object_id";

    public static final String COL_OBJECT_TYPE = "object_type";

    public static final String COL_METADATA = "metadata";

    public static final String COL_USER_METADATA = "user_metadata";

    public static final String COL_TH_METADATA = "th_metadata";

    public static final String COL_TH_USER_METADATA = "th_user_metadata";

    public static final String COL_ATTR = "attr";

    public static final String COL_HASH_INFO = "hash_info";

    public static final String COL_UPLOAD_ID = "upload_id";

    public static final String COL_UPLOAD_STATUS = "upload_status";

    public static final String COL_CREATE_TIME = "create_time";


}

