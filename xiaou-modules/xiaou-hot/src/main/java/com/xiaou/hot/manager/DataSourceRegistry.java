package com.xiaou.hot.manager;


import com.xiaou.hot.datasource.*;
import com.xiaou.hot.model.enums.HotDataKeyEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源注册表
 */
@Component
@RequiredArgsConstructor
public class DataSourceRegistry {


    private Map<String, DataSource> typeDataSourceMap;

    private final ZhiHuDataSource zhiHuDataSource;
    private final WeiBoDataSource weiBoDataSource;
    private final CodeFatherDataSource codeFatherDataSource;
    private final BiliBiliDataSource bilibiliDataSource;
    private final DouYinDataSource douYinDataSource;
    private final WYCloudDataSource wyCloudDataSource;
    private final CsdnDataSource csdnDataSource;
    private final JueJinDataSource jueJinDataSource;


    @PostConstruct
    public void doInit() {
        typeDataSourceMap = new HashMap<String, DataSource>() {{
            put(HotDataKeyEnum.ZHI_HU.getValue(), zhiHuDataSource);
            put(HotDataKeyEnum.WEI_BO.getValue(), weiBoDataSource);
            put(HotDataKeyEnum.CODE_FATHER.getValue(), codeFatherDataSource);
            put(HotDataKeyEnum.BILI_BILI.getValue(), bilibiliDataSource);
            put(HotDataKeyEnum.WY_CLOUD_MUSIC.getValue(), wyCloudDataSource);
            put(HotDataKeyEnum.DOU_YIN.getValue(), douYinDataSource);
            put(HotDataKeyEnum.CS_DN.getValue(), csdnDataSource);
            put(HotDataKeyEnum.JUE_JIN.getValue(), jueJinDataSource);
        }};
    }


    public DataSource getDataSourceByType(String type) {
        if (typeDataSourceMap == null) {
            return null;
        }
        return typeDataSourceMap.get(type);
    }
}