package com.xiaou.hot.datasource;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.enums.UpdateIntervalEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * csdn 热榜数据源
 */
@Slf4j
@Component
public class CsdnDataSource implements DataSource {

    private static final String CSDN_HOT_URL = "https://blog.csdn.net/phoenix/web/blog/hot-rank";

    @Override
    public HotPost getHotPost() {
        int pageSize = 25;
        int pagesNeeded = 3;
        List<HotPostDataVO> allDataList = new ArrayList<>();

        for (int page = 0; page < pagesNeeded; page++) {
            String url = CSDN_HOT_URL + "?page=" + page + "&pageSize=" + pageSize + "&type=";

            try {
                // 发送 GET 请求并获取 JSON 响应
                String result = HttpRequest.get(url).execute().body();
                JSONObject resultJson = JSON.parseObject(result);
                JSONArray data = resultJson.getJSONArray("data");
                // 解析数据并转换为 VO 对象
                List<HotPostDataVO> dataList = data.stream().map(item -> {
                    JSONObject jsonItem = (JSONObject) item;
                    String title = Optional.ofNullable(jsonItem.getString("articleTitle")).orElse("");
                    String articleDetailUrl = jsonItem.getString("articleDetailUrl");
                    String hotRankScore = Optional.ofNullable(jsonItem.getString("hotRankScore")).orElse("0");

                    return HotPostDataVO.builder()
                            .title(title)
                            .url(articleDetailUrl)
                            .followerCount(parseHotRankScore(hotRankScore))
                            .build();
                }).collect(Collectors.toList());
                allDataList.addAll(dataList);
            } catch (Exception e) {
                log.error("CSDN 热榜数据获取失败", e);
            }

        }

        List<HotPostDataVO> sortedDataList = allDataList.stream()
                .sorted(Comparator.comparingInt(HotPostDataVO::getFollowerCount).reversed())
                .collect(Collectors.toList());

        return HotPost.builder()
                .sort(CategoryTypeEnum.TECH_PROGRAMMING.getValue())
                .category(CategoryTypeEnum.TECH_PROGRAMMING.getValue())
                .name("CSDN热榜")
                .updateInterval(UpdateIntervalEnum.HALF_HOUR.getValue())
                .iconUrl("https://blog.csdn.net/favicon.ico")
                // 取前 20 条数据
                .hostJson(JSON.toJSONString(sortedDataList.subList(0, Math.min(sortedDataList.size(), 20))))
                .typeName("CSDN")
                .build();
    }

    /**
     * 解析 hotRankScore，确保其为整数
     *
     * @param hotRankScore 纯数字字符串
     * @return 转换后的整数值
     */
    private int parseHotRankScore(String hotRankScore) {
        try {
            return Integer.parseInt(hotRankScore);
        } catch (NumberFormatException e) {
            log.warn("Invalid hotRankScore format: {}", hotRankScore);
            return 0;
        }
    }
}