package com.xiaou.hot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.hot.mapper.HotPostMapper;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.model.vo.HotPostVO;
import com.xiaou.hot.service.HotPostService;
import com.xiaou.utils.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HotPostServiceImpl extends ServiceImpl<HotPostMapper, HotPost> implements HotPostService {


    private static final String HOT_POST_CACHE_KEY = "hot:post";
    // 缓存时间 30 分钟
    private static final long CACHE_EXPIRE_TIME = 30;

    @Override
    public List<HotPostVO> getHotPostList() {
        // 1. 尝试从 Redis 读取 List<HotPostVO>，直接用 RedisUtils 的 getCacheList（存的是对象，不用自己序列化）
        List<HotPostVO> hotPostList = RedisUtils.getCacheList(HOT_POST_CACHE_KEY);
        if (hotPostList != null && !hotPostList.isEmpty()) {
            return hotPostList;
        }
        // 2. Redis没数据，则查询数据库
        hotPostList = this.list(new LambdaQueryWrapper<HotPost>().orderByAsc(HotPost::getSort))
                .stream().map(HotPostVO::objToVo).collect(Collectors.toList());

        // 3. 缓存到 Redis，存List，设置过期时间，RedisUtils 里面没有带过期时间的setCacheList，这里自己设置过期时间：
        // 可以先setCacheList，再expire
        RedisUtils.setCacheList(HOT_POST_CACHE_KEY, hotPostList);
        RedisUtils.expire(HOT_POST_CACHE_KEY, Duration.ofMinutes(CACHE_EXPIRE_TIME));

        // 4. 返回数据
        return hotPostList;
    }

}


