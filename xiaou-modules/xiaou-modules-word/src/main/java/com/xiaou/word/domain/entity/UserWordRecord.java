package com.xiaou.word.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户单词学习记录表
 * @TableName u_user_word_record
 */
@TableName(value ="u_user_word_record")
@Data
public class UserWordRecord {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    private String  userId;

    /**
     * 单词ID
     */
    private String wordId;

    /**
     * 认识次数
     */
    private Integer knowCount;

    /**
     * 不认识次数
     */
    private Integer notKnowCount;

    /**
     * 最后学习时间
     */
    private Date lastStudyTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}