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
                .header("cookie", "_xsrf=aZ6GfLnrwOwnhTvmSivJWW8pVPJCN0Kn; _zap=6afaa3c6-4bd3-43b9-ba5c-5af2fd39c280; d_c0=XabTjRL5ExqPTsRBMAucBjIU9-LP-xDmTyM=|1740796960; __snaker__id=3yNJyZHHYtU3ik60; q_c1=6bb6ee56f0d743958b728185eb3fd1d3|1741310218000|1741310218000; tst=r; z_c0=2|1:0|10:1747882940|4:z_c0|80:MS4xMzRVOFd3QUFBQUFtQUFBQVlBSlZUZEx6QzJucWxVMjJ1RldwS2ZTS1c4dDNZRjdiMHRMeEpRPT0=|2cf78b929169a91623546edee29f21e6ecb49230f0a4a1c3212ffea41556fa90; __zse_ck=004_ZdBHaHv=UG6vgXq12=yaXIvf=6e3LB3aNxjdsR8YG/CbfDk6v3snYUyVLhyh8sOo213GB8uWktZDu6qpR3S0n5hFOtanIWISy3nZh7clZ0kIlo1C7PWTJ5rAIwSSqC0G-SZ8vMeaf1CImNKGQ6lJB9Q8101m4aCPXptsqfzEWZTjELWWBYX10Y3x6B0xHzUP65o2oTS2rXwd/ZT42D9IIfeNBPv5hSs7hg+Ykai+oJGYY/r94JXmsdBRAFznl/7m4; Hm_lvt_98beee57fd2ef70ccdd5ca52b9740c49=1746842947,1747793325,1747882939,1748417209; BEC=32377ec81629ec05d48c98f32428ae46")
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
