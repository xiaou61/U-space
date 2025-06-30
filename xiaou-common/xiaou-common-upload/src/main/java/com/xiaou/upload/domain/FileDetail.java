package com.xiaou.upload.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
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


    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String url;


    private Long size;

    private String filename;


    private String originalFilename;


    private String basePath;


    private String path;


    private String ext;


    private String contentType;


    private String platform;



    private String thUrl;


    private String thFilename;



    private Long thSize;



    private String thContentType;



    private String objectId;



    private String objectType;



    private String metadata;



    private String userMetadata;



    private String thMetadata;



    private String thUserMetadata;


    private String attr;


    private String fileAcl;



    private String thFileAcl;



    private String hashInfo;



    private String uploadId;


    private Integer uploadStatus;



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

