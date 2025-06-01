package com.xiaou.hot.datasource;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.enums.UpdateIntervalEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostDataVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编程导航数据源
 */
@Slf4j
@Component
public class CodeFatherDataSource implements DataSource {
    @Override
    public HotPost getHotPost() {
        String urlCodeFather = "https://api.codefather.cn/api/search/hot";
        HttpPost request = new HttpPost(urlCodeFather);
        // 创建请求体（body）
        String jsonBody = "{\"hiddenContent\": true, \"pageSize\": 20, \"type\": \"all_hot\"}";
        // 添加常见的请求头
        request.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        request.setHeader(HttpHeaders.REFERER, "https://s.weibo.com/top/summary?cate=realtimehot");

        // 添加其他请求头
        request.setHeader("Access-Control-Allow-Credentials", "true");
        request.setHeader("Access-Control-Allow-Origin", "https://www.codefather.cn");
        request.setHeader("Access-Control-Expose-Headers", "*");
        request.setHeader("Content-Encoding", "gzip");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Date", "Fri, 21 Feb 2025 07:03:51 GMT");
        request.setHeader("Server", "www.tsycdn.com");
        request.setHeader("Strict-Transport-Security", "max-age=31536000");
        request.setHeader("Vary", "Accept-Encoding");
        request.setHeader("Vary", "Origin,Access-Control-Request-Method,Access-Control-Request-Headers");
        request.setHeader("X-Cloudbase-Request-Id", "af099400-1c70-4f1b-9e6c-9a67996adce4");
        request.setHeader("X-Cloudbase-Upstream-Status-Code", "200");
        request.setHeader("X-Cloudbase-Upstream-Timecost", "133");
        request.setHeader("X-Cloudbase-Upstream-Type", "Tencent-CBR");
        request.setHeader("X-Request-Id", "af099400-1c70-4f1b-9e6c-9a67996adce4");
        request.setHeader("X-Upstream-Status-Code", "200");
        request.setHeader("X-Upstream-Timecost", "133");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        List<HotPostDataVO> dataList = new ArrayList<>();
        try {
            StringEntity entity = new StringEntity(jsonBody);

            // 设置请求体
            request.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject resultJson = (JSONObject) JSON.parse(result);
            JSONObject data = resultJson.getJSONObject("data");
            JSONArray records = data.getJSONObject("searchPage").getJSONArray("records");
            records.forEach(item -> {
                JSONObject jsonItem = (JSONObject) item;
                String title = jsonItem.getString("title");
                String content = jsonItem.getString("content");
                String recommendScore = jsonItem.getString("recommendScore");
                String id = jsonItem.getString("id");
                String url = "https://www.codefather.cn/" + (CharSequenceUtil.isBlank(title) ? "essay" : "post") + "/" + id;
                HotPostDataVO dataVO = HotPostDataVO.builder()
                        .title(CharSequenceUtil.isBlank(title) ? content.substring(0, 20) : title)
                        .url(url)
                        .followerCount(Integer.parseInt(extractNumber(recommendScore)) * 10)
                        .build();
                dataList.add(dataVO);
            });
        } catch (Exception e) {
            log.error("编程导航数据源获取失败", e);
        }

        return HotPost.builder()
                .sort(CategoryTypeEnum.TECH_PROGRAMMING.getValue())
                .name("编程热门")
                .category(CategoryTypeEnum.TECH_PROGRAMMING.getValue())
                .updateInterval(UpdateIntervalEnum.ONE_DAY.getValue())
                .iconUrl("https://www.codefather.cn/favicon.ico")
                //只拿前 20 条数据
                .hostJson(JSON.toJSONString(dataList.subList(0, Math.min(dataList.size(), 20))))
                .typeName("编程导航")
                .build();
    }

    public static String extractNumber(String input) {
        // 正则表达式匹配数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }
}
