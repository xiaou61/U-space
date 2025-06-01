package com.xiaou.hot.datasource;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.enums.UpdateIntervalEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostDataVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.xiaou.hot.datasource.CodeFatherDataSource.extractNumber;

/**
 * 微博热榜数据源
 *
 * @author cong
 * @date 2025/02/21
 */
@Slf4j
@Component
public class WeiBoDataSource implements DataSource {
    @Override
    public HotPost getHotPost() {
        //获取tid
        String tidUrl = "https://passport.weibo.com/visitor/genvisitor";
        Map<String, Object> params = new HashMap<>();
        params.put("cb", "gen_callback");
        String str = HttpUtil.get(tidUrl, params, 3000);
        String quStr = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        String tid = "";
        if (!quStr.isEmpty()) {
            JSONObject result = JSON.parseObject(quStr);
            if (result.getIntValue("retcode") == 20000000) {
                tid = result.getJSONObject("data").getString("tid");
            }
        }

        //获腹SUb,sUbp
        String subUrl = "https://passport.weibo.com/visitor/visitor";
        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", "incarnate");
        params2.put("t", tid);
        params2.put("w", "3");
        params2.put("c", "100");
        params2.put("cb", "cross_domain");
        params2.put("from", "weibo");
        String str2 = HttpUtil.get(subUrl, params2, 3000);
        String resultStr = str2.substring(str2.indexOf("(") + 1, str2.indexOf(")"));
        String sub = "";
        String subp = "";
        if (!resultStr.isEmpty()) {
            JSONObject result = JSON.parseObject(resultStr);
            if (result.getIntValue("retcode") == 20000000) {
                sub = result.getJSONObject("data").getString("sub");
                subp = result.getJSONObject("data").getString("subp");
            }
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        HttpGet request = new HttpGet("https://s.weibo.com/top/summary?cate=realtimehot");
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        request.setHeader("Referer", "https://s.weibo.com/top/summary?cate=realtimehot");
        request.setHeader("Cookie", "SUB=" + sub + "; SUBP=" + subp + ";");

        List<HotPostDataVO> dataList = new ArrayList<>();
        try {
            response = httpClient.execute(request);
            String html = EntityUtils.toString(response.getEntity());

            Document document = Jsoup.parse(html);
            Element item = document.getElementsByTag("tbody").first();
            if (item != null) {
                String tdKey = ".td-02";
                Elements items = item.getElementsByTag("tr");
                for (Element tmp : items) {
                    Element rankEle = tmp.getElementsByTag("td").first();
                    Elements textEle = tmp.select(tdKey).select("a");
                    Elements followerEle = tmp.select(tdKey).select("span");
                    //过滤广告
                    Elements rdEle = tmp.select(tdKey).select("span");
                    if (!Objects.requireNonNull(rankEle).text().isEmpty() && !rdEle.text().isEmpty()) {
                        HotPostDataVO dataVO = HotPostDataVO.builder()
                                .title(textEle.text())
                                .url("https://s.weibo.com" + textEle.attr("href"))
                                .followerCount(Integer.valueOf(extractNumber(followerEle.text())))
                                .build();
                        dataList.add(dataVO);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取微博热搜失败", e);
        }

        return HotPost.builder()
                .sort(CategoryTypeEnum.GENERAL_DISCUSSION.getValue())
                .name("微博热搜")
                .category(CategoryTypeEnum.GENERAL_DISCUSSION.getValue())
                .updateInterval(UpdateIntervalEnum.HALF_HOUR.getValue())
                .iconUrl("https://s.weibo.com/favicon.ico")
                .hostJson(JSON.toJSONString(dataList.subList(0, Math.min(dataList.size(), 20))))
                .typeName("微博")
                .build();
    }


}
