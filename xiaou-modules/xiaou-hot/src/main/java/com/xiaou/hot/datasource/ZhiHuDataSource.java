package com.xiaou.hot.datasource;

import cn.hutool.http.HttpRequest;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaou.common.utils.StringUtils;
import com.xiaou.hot.model.enums.CategoryTypeEnum;
import com.xiaou.hot.model.enums.UpdateIntervalEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知乎热榜数据源
 */
@Slf4j
@Component
public class ZhiHuDataSource implements DataSource {
    @Override
    public HotPost getHotPost() {
        String urlZhiHu = "https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=50&desktop=true";
        //带上请求头
        String result = HttpRequest.get(urlZhiHu)
                .header("cookie", "_xsrf=VN96XyHRESB738GJVA30aJpWs9iN5DZi; _zap=06bee125-d912-48bb-9268-a20bc0e082f4; d_c0=AACSIeP7GxmPTrLfEYb8pFFlDMCr0B6-pgY=|1724154699; __snaker__id=bLAEX3gz29CFPiwb; q_c1=f2a6f5588c8b4405995f66908ca721c0|1740453050000|1740453050000; z_c0=2|1:0|10:1745735104|4:z_c0|80:MS4xUThEb0R3QUFBQUFtQUFBQVlBSlZUY0FiLTJnVkUxc3p2ZF9mZ2ZrZ09BQzk5QUJ6aXpXM0R3PT0=|342ba90896869fd4c624fd72d9713b900697e5af55a28e9744eb641d57fbceb8; tst=r; __zse_ck=004_/yVfDVIRfmBxlQ80NWNy8Rmq36dTtvQAvqxgMVYiQBHwCVKp3BS=2HJ5es8i1PwBhOnE6jaNQrqLA0cxI5Bjnr6jAYqgEqvNsljMp9phkL/AwW7aKHEg6kQwQgyPYNuM-oCXOyo+Pryalr24skc5gdVekNL89t2gmyLzKeIQDYtX8Ec6sPBm4tWMi2qPn1Vx3KlhFNRriQl42VcWtYxw8EaywywGGOFUWm+NCaHJdDNiDtNbDylMPJtuD+rFbUUm5; Hm_lvt_98beee57fd2ef70ccdd5ca52b9740c49=1745735105,1745800809,1746276491,1746581413; Hm_lpvt_98beee57fd2ef70ccdd5ca52b9740c49=1746581413; HMACCOUNT=DC7B5A5643487E58; BEC=b7b0f394f3fd074c6bdd2ebbdd598b4e")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("referer", "https://www.zhihu.com/hot")
                .header("accept", "application/json, text/plain, */*")
                .header("accept-language", "zh-CN,zh;q=0.9")
                .execute()
                .body();
        JSONObject resultJson = (JSONObject) JSON.parse(result);
        JSONArray data = resultJson.getJSONArray("data");
        List<HotPostDataVO> dataList = data.stream().map(item -> {
            JSONObject jsonItem = (JSONObject) item;
            JSONObject target = jsonItem.getJSONObject("target");
            String title = target.getString("title");
            String[] parts = target.getString("url").split("/");
            String url = "https://zhihu.com/question/" + parts[parts.length - 1];
            String followerCount = jsonItem.getString("detail_text");

            return HotPostDataVO.builder()
                    .title(title)
                    .url(url)
                    .followerCount(Integer.parseInt(StringUtils.extractNumber(followerCount)) * 10000)
                    .build();
        }).collect(Collectors.toList());
        return HotPost.builder()
                .sort(CategoryTypeEnum.GENERAL_DISCUSSION.getValue())
                .category(CategoryTypeEnum.GENERAL_DISCUSSION.getValue())
                .name("知乎热榜")
                .updateInterval(UpdateIntervalEnum.HALF_HOUR.getValue())
                .iconUrl("https://www.zhihu.com/favicon.ico")
                .hostJson(JSON.toJSONString(dataList.subList(0, Math.min(dataList.size(), 20))))
                .typeName("知乎")
                .build();
    }
}
