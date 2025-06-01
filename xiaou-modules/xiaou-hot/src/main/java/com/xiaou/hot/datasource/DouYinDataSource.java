package com.xiaou.hot.datasource;


import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.enums.UpdateIntervalEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostDataVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抖音数据源
 */
@Component
@Slf4j
public class DouYinDataSource implements DataSource {

    @Override
    public HotPost getHotPost() {
        final String HOT_BASE_URL = "https://www.douyin.com/hot";
        final String API_URL = "https://www.douyin.com/aweme/v1/web/hot/search/list";
        String fullApiUrl;
        try {
            URIBuilder uriBuilder = new URIBuilder(API_URL)
                    // 添加设备平台参数，值为"webapp"
                    .addParameter("device_platform", "webapp")
                    // 添加应用ID参数，值为"6383"
                    .addParameter("aid", "6383")
                    // 添加渠道参数，值为"channel_pc_web"
                    .addParameter("channel", "channel_pc_web")
                    // 添加详情列表参数，值为"1"
                    .addParameter("detail_list", "1")
                    // 添加来源参数，值为"6"
                    .addParameter("source", "6")
                    // 添加PC客户端类型参数，值为"1"
                    .addParameter("pc_client_type", "1")
                    // 添加PC天秤座参数，值为"Windows"
                    .addParameter("pc_libra_divert", "Windows")
                    // 添加是否支持H.265编码参数，值为"1"
                    .addParameter("support_h265", "1")
                    // 添加是否支持DASH流媒体参数，值为"1"
                    .addParameter("support_dash", "1")
                    // 添加版本代码参数，值为"170400"
                    .addParameter("version_code", "170400")
                    // 添加版本名称参数，值为"17.4.0"
                    .addParameter("version_name", "17.4.0")
                    // 添加Cookie启用状态参数，值为"true"
                    .addParameter("cookie_enabled", "true")
                    // 添加屏幕宽度参数，值为"2560"
                    .addParameter("screen_width", "2560")
                    // 添加屏幕高度参数，值为"1440"
                    .addParameter("screen_height", "1440")
                    // 添加浏览器语言参数，值为"zh-CN"
                    .addParameter("browser_language", "zh-CN")
                    // 添加浏览器平台参数，值为"Win32"
                    .addParameter("browser_platform", "Win32")
                    // 添加浏览器名称参数，值为"Edge"
                    .addParameter("browser_name", "Edge")
                    // 添加浏览器版本参数，值为"133.0.0.0"
                    .addParameter("browser_version", "133.0.0.0")
                    // 添加浏览器在线状态参数，值为"true"
                    .addParameter("browser_online", "true")
                    // 添加引擎名称参数，值为"Blink"
                    .addParameter("engine_name", "Blink")
                    // 添加引擎版本参数，值为"133.0.0.0"
                    .addParameter("engine_version", "133.0.0.0")
                    // 添加操作系统名称参数，值为"Windows"
                    .addParameter("os_name", "Windows")
                    // 添加操作系统版本参数，值为"10"
                    .addParameter("os_version", "10")
                    // 添加CPU核心数参数，值为"6"
                    .addParameter("cpu_core_num", "6")
                    // 添加设备内存参数，值为"8"
                    .addParameter("device_memory", "8")
                    // 添加平台参数，值为"PC"
                    .addParameter("platform", "PC")
                    // 添加下行带宽参数，值为"10"
                    .addParameter("downlink", "10")
                    // 添加有效连接类型参数，值为"4g"
                    .addParameter("effective_type", "4g")
                    // 添加往返时间参数，值为"50"
                    .addParameter("round_trip_time", "50")
                    // 添加WebID参数，值为"7459028680137016871"
                    .addParameter("webid", "7459028680137016871")
                    // 添加验证指纹参数，值为"verify_m5tpgw3p_LB5ogmKL_7J3D_4tHA_9KhF_sKSSnvRE2ahC"
                    .addParameter("verifyFp", "verify_m5tpgw3p_LB5ogmKL_7J3D_4tHA_9KhF_sKSSnvRE2ahC")
                    // 添加指纹参数，值为"verify_m5tpgw3p_LB5ogmKL_7J3D_4tHA_9KhF_sKSSnvRE2ahC"
                    .addParameter("fp", "verify_m5tpgw3p_LB5ogmKL_7J3D_4tHA_9KhF_sKSSnvRE2ahC")
                    // 添加msToken参数，值为经过加密的字符串
                    .addParameter("msToken", "JpZVrvVrfW7Ldi9XMQZ2dbMAISCb0nbz1a2IHlQRHe-9Sdev3AX_4o1_s_NoaCeSNGphF9Oq1LqmJi0a4LsE1RjGqNQcCZ6U98HEzAjStwUHhrupMU-Ur6r3CDjConG_X6JwJwoh6DQyxRzd9Ep60i1PtS85gAMjQPuwmCqL2mFoXHR1YJJ3GFs%3D")
                    // 添加a_bogus参数，值为经过加密的字符串
                    .addParameter("a_bogus", "mysfhqW7EqmjedKtmOG-CaQlJMjMNsSyfBioRYqTtPOYTHzbiSNZgabcnoK1si5wzYBTi937sDtlbEnc%2FsX0ZFHpomkvuhkjBzIAV06ohqqgTehBLrRqCuzitJGbWmiEm5ofJlWUItQcIEK4DZrhUdAyyAkisYJpKNabdrRaY9tD6zT9BrqQuPSdxwtq4E%3D%3D");

            fullApiUrl = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            log.error("URL 构造异常: {}", e.getMessage(), e);
            return buildHotPost(Collections.emptyList());
        }
        final int TIMEOUT = 30_000;

        List<HotPostDataVO> dataList = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(10_000)
                        .setSocketTimeout(TIMEOUT)
                        .build())
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .setContentDecoderRegistry(Collections.singletonMap("gzip", GZIPInputStreamFactory.getInstance()))
                .build()) {

            HttpGet httpGet = new HttpGet(fullApiUrl);
            configureHeaders(httpGet);

            try (CloseableHttpResponse response = client.execute(httpGet)) {
                dataList = processResponse(response, HOT_BASE_URL);
            }
        } catch (IOException e) {
            log.error("网络通信异常: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("系统异常: {}", e.getMessage(), e);
        }

        return buildHotPost(dataList);
    }

    private void configureHeaders(HttpGet httpGet) {
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)...");
        httpGet.setHeader("Referer", "https://www.douyin.com/hot");
        httpGet.setHeader("Accept", "application/json");
    }

    private List<HotPostDataVO> processResponse(CloseableHttpResponse response, String hotBaseUrl) throws IOException {
        String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonNode rootNode = new ObjectMapper().readTree(responseString);
        return convertAndSortItems(rootNode.path("data").path("word_list"), hotBaseUrl);
    }

    private List<HotPostDataVO> convertAndSortItems(JsonNode items, String hotBaseUrl) {
        List<JsonNode> sortedItems = new ArrayList<>();
        items.forEach(sortedItems::add);

        // 按 `position` 升序排序
        sortedItems.sort(Comparator.comparingInt(item -> item.path("position").asInt()));

        return sortedItems.stream()
                .filter(this::validateItem)
                .map(item -> convertToVO(item, hotBaseUrl))
                .collect(Collectors.toList());
    }

    private boolean validateItem(JsonNode item) {
        return item.has("word") && item.has("hot_value") && item.has("sentence_id");
    }

    private HotPostDataVO convertToVO(JsonNode item, String hotBaseUrl) {
        try {
            String title = item.get("word").asText();
            String sentenceId = item.get("sentence_id").asText();
            int followerCount = item.get("hot_value").asInt();

            // 拼接 URL
            String encodedTitle = URLEncoder.encode(title, "UTF-8").replace("+", "%20");
            String url = String.format("%s/%s/%s", hotBaseUrl, sentenceId, encodedTitle);


            return HotPostDataVO.builder()
                    .title(title)
                    .url(url)
                    .followerCount(followerCount)
                    .build();

        } catch (Exception e) {
            log.error("数据转换异常: {}", e.getMessage(), e);
            return null;
        }
    }

    private HotPost buildHotPost(List<HotPostDataVO> dataList) {
        return HotPost.builder()
                .sort(CategoryTypeEnum.VIDEO_ENTERTAINMENT.getValue())
                .name("抖音热搜")
                .category(CategoryTypeEnum.VIDEO_ENTERTAINMENT.getValue())
                .updateInterval(UpdateIntervalEnum.HALF_HOUR.getValue())
                .iconUrl("https://lf1-cdn-tos.bytegoofy.com/goofy/ies/douyin_web/public/favicon.ico")
                // 取前 20 条数据
                .hostJson(JSON.toJSONString(dataList.subList(0, Math.min(dataList.size(), 20))))
                .typeName("抖音")
                .build();
    }
}
