package com.xiaou.hot.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaou.hot.manager.DataSourceRegistry;
import com.xiaou.hot.model.enums.HotDataKeyEnum;
import com.xiaou.hot.model.po.HotPost;
import com.xiaou.hot.service.HotPostService;
import com.xiaou.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 自动异步同步热榜数据
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IncSyncHostPostToMySQL {

    private final DataSourceRegistry dataSourceRegistry;
    private final HotPostService hotPostService;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;


    private static final String HOT_POST_CACHE_KEY = "hot_post_list";
    /**
     * 应用启动时执行一次
     */
    @PostConstruct
    public void initRun() {
        log.info("应用启动，立即执行一次更新热榜任务...");
        // 通过线程池执行延迟任务（JDK 9+）
        CompletableFuture.runAsync(this::run, CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS, threadPoolTaskExecutor));
    }

    /**
     * 每半小时执行一次，异步更新各类型热榜数据
     */
    @Scheduled(fixedRate = 1_800_000)
    public void run() {
        log.info("开始更新热榜数据...");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String key : HotDataKeyEnum.getValues()) {
            // 异步执行更新任务，使用注入的线程池
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                updateHotPostWithRetry(key);
            }, threadPoolTaskExecutor);
            futures.add(future);
        }

        // 等待所有异步任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        stopWatch.stop();
        log.info("更新热榜数据完成，耗时：{}ms", stopWatch.getTotalTimeMillis());
    }

    /**
     * 带重试机制的更新热榜任务
     */
    private void updateHotPostWithRetry(String key) {
        int maxAttempts = 3;
        boolean success = false;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                updateHotPost(key);
                success = true;
                break;
            } catch (Exception e) {
                log.warn("第 {} 次尝试更新热榜【{}】失败，原因: {}", attempt, key, e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }

        if (!success) {
            log.error("更新热榜数据失败，达到最大重试次数，放弃更新【{}】", key);
        }
    }

    /**
     * 单个热榜数据的更新逻辑
     */
    private void updateHotPost(String key) {
        LambdaQueryWrapper<HotPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotPost::getType, key);

        HotPost oldHotPost = hotPostService.getOne(queryWrapper);
        if (oldHotPost != null) {
            BigDecimal updateInterval = oldHotPost.getUpdateInterval();
            long updateIntervalMillis = updateInterval.multiply(new BigDecimal(60 * 60 * 1000)).longValue();

            if (oldHotPost.getUpdateTime().getTime() + updateIntervalMillis > System.currentTimeMillis()) {
                log.info("加载===========>【{}】热榜数据跳过", HotDataKeyEnum.getEnumByValue(key).getText());
                return;
            }
        }

        HotPost hotPost = dataSourceRegistry.getDataSourceByType(key).getHotPost();
        hotPost.setType(key);
        if (oldHotPost != null) {
            hotPost.setId(oldHotPost.getId());
        }
        hotPost.setUpdateTime(new Date());

        hotPostService.saveOrUpdate(hotPost);
        log.info("加载===========>【{}】热榜数据完成", hotPost.getTypeName());
    }
}