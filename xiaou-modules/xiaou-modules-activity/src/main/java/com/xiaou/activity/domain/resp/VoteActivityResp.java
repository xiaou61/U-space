package com.xiaou.activity.domain.resp;

import com.xiaou.activity.domain.entity.VoteOption;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VoteActivityResp {
    private String id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态（1=进行中，0=已结束，-1=草稿）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 关联的一个options
     */
    private List<VoteOption> options;
}
