package com.xiaou.campus.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("u_qa_pairs")
public class QaPairs {
    @TableId
    private Long id;

    /**
     *
     */
    private String q;

    /**
     *
     */
    private String a;
}
