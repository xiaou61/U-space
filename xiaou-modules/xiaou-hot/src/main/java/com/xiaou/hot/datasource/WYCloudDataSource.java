package com.xiaou.hot.datasource;


import com.alibaba.fastjson2.JSON;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cong
 */
@Slf4j
@Component
public class WYCloudDataSource implements DataSource {
    @Override
    public HotPost getHotPost() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        HttpGet request = new HttpGet("https://music.163.com/discover/toplist?id=3778678");
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

        try {
            response = httpClient.execute(request);
            String html = EntityUtils.toString(response.getEntity());
            Document document = Jsoup.parse(html);
            // 找到热门歌单class f-hide
            Element first = document.getElementsByClass("f-hide").first();
            List<HotPostDataVO> dataList = first.select("a").stream().map(item -> {
                String title = item.text();
                String url = item.attr("href");
                String[] urlArray = url.split("=");
                return HotPostDataVO.builder()
                        .title(title)
//                        .url("https://music.163.com" + url)
                        .url("//music.163.com/outchain/player?type=2&id=" + urlArray[1] + "&auto=0&height=66")
                        .build();
            }).collect(Collectors.toList());

            return HotPost.builder()
                    .sort(CategoryTypeEnum.MUSIC_HOT.getValue())
                    .category(CategoryTypeEnum.MUSIC_HOT.getValue())
                    .name("网易云热歌榜")
                    .updateInterval(UpdateIntervalEnum.TWO_HOUR.getValue())
                    .iconUrl("https://s1.aigei.com/src/img/png/6c/6c2a6e0d311c4df8b479c7e998245840.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:lEH71zFLIFDtxkPgdrHHVsRvgEU=")
                    .hostJson(JSON.toJSONString(dataList.subList(0, Math.min(dataList.size(), 20))))
                    .typeName("网易云")
                    .build();

        } catch (Exception e) {
            log.error("获取网易云热歌榜", e);
        }
        return null;
    }
}
