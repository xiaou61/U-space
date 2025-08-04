package com.xiaou.word.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.word.domain.entity.UserWordRecord;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 用户单词学习记录表
 * @TableName u_user_word_record
 */
@Data
@AutoMapper(target = UserWordRecord.class)
public class UserWordRecordResp {
    /**
     * 主键ID
     */
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
     * 新增字段
     */
    private String word;

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
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}