package com.xiaou.room.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.room.domain.entity.DormBed;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

@Data
public class DormBedResp {
    /**
     * 床位编号，如"1号床"
     */
    private String bedNumber;

    /**
     * 床位状态（0-未被选择，1-已被抢占）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
