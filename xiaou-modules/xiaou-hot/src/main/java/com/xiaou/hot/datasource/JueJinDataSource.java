package com.xiaou.hot.datasource;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.enums.UpdateIntervalEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostDataVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 掘金数据源
 *
 * @author shing
 */
@Slf4j
@Component
public class JueJinDataSource implements DataSource {

    private static final String JUE_JIN_URL = "https://api.juejin.cn/content_api/v1/content/article_rank";

    private static final String JUE_JIN_POST_URL = "https://juejin.cn/post/";

    @Override
    public HotPost getHotPost() {
        List<HotPostDataVO> allDataList = new ArrayList<>();

        try {
            // 1. 构建请求URL
            URI url = new URIBuilder(JUE_JIN_URL)
                    .addParameter("category_id", "1")
                    .addParameter("type", "hot")
                    .addParameter("aid", "2608")
                    .addParameter("uuid", "7452631964433958441")
                    .addParameter("spider", "0")
                    .build();

            // 2. 发送请求并处理响应
            try (HttpResponse response = HttpRequest.get(url.toString())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36...")
                    .execute()) {

                String result = response.body();
                JSONObject resultJson = JSON.parseObject(result);
                JSONArray data = resultJson.getJSONArray("data");

                // 3. 解析数据
                data.stream()
                        .map(JSONObject.class::cast)
                        .forEach(jsonItem -> {
                            try {
                                JSONObject content = jsonItem.getJSONObject("content");
                                JSONObject contentCounter = jsonItem.getJSONObject("content_counter");

                                String title = content.getString("title");
                                String contentId = content.getString("content_id");
                                int hotRank = contentCounter.getIntValue("hot_rank");

                                allDataList.add(HotPostDataVO.builder()
                                        .title(title)
                                        .url(JUE_JIN_POST_URL + contentId)
                                        .followerCount(hotRank)
                                        .build());

                            } catch (Exception e) {
                                log.warn("数据解析失败: {}", jsonItem.toJSONString(), e);
                            }
                        });
            }

        } catch (URISyntaxException e) {
            log.error("URL构造失败: {}", JUE_JIN_URL, e);
        } catch (Exception e) {
            log.error("未知错误", e);
        }

        // 4. 排序并返回
        return HotPost.builder()
                .category(CategoryTypeEnum.TECH_PROGRAMMING.getValue())
                .sort(CategoryTypeEnum.TECH_PROGRAMMING.getValue())
                .name("掘金热榜")
                .updateInterval(UpdateIntervalEnum.HALF_HOUR.getValue())
                .iconUrl("https://lf3-cdn-tos.bytescm.com/obj/static/xitu_juejin_web//static/favicon.ico")
                .hostJson(JSON.toJSONString(allDataList.stream()
                        .sorted(Comparator.comparingInt(HotPostDataVO::getFollowerCount).reversed())
                        .collect(Collectors.toList())
                        .subList(0, Math.min(allDataList.size(), 20))))
                .typeName("掘金")
                .build();
    }
}