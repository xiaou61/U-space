package com.xiaou.hot.model;

import lombok.Data;

@Data
public class TorderItem {
    private Integer id;
    private Integer oid;
    private Integer pid;
    private String title;
    private String image;
    private Integer price;
    private Integer num;
    private String createdTime;
    private String modifiedTime;
    private String createdUser;
    private String modifiedUser;
}
