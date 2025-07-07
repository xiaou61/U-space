package com.xiaou.index.domain.entity;

import lombok.Data;

@Data
public class Weather  {


    private String city;//地区
    private String time;//天气上一次更新时间
    private String shidu;//湿度
    private String quality;//空气质量
    private String wendu;//温度

    private String high;//最高温度
    private String low;//最低温度
    private String sunrise;//日出时间
    private String sunset;//日落时间
    private String aqi;//空气指数
    private String fx;//风向
    private String fl;//风速
    private String type;//天气
    private String notice;//温馨提醒

}
