package com.xiaou.system.bulletin.domain.req;

import com.xiaou.system.bulletin.domain.entity.Announcement;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Announcement.class)
public class AnnouncementReq {

    private String id;

    /**
     * 公告内容
     */
    private String content;


    /**
     * 是否需要弹框提醒（0否，1是）
     */
    private Integer needPopup;

}
