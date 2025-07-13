package com.xiaou.bbs.job;

import com.xiaou.bbs.domain.entity.BbsPost;
import com.xiaou.bbs.domain.entity.PostRecommendScore;
import com.xiaou.bbs.mapper.BbsPostMapper;
import com.xiaou.bbs.mapper.PostRecommendScoreMapper;
import com.xiaou.bbs.service.PostRecommendScoreService;
import com.xiaou.redis.utils.RedisUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PostRecommendJob {
    @Resource
    private BbsPostMapper postMapper;
    @Resource
    private PostRecommendScoreService postRecommendScoreService;

    @XxlJob("PostRecommendJob")
    public void execute() {
        //提取帖子 近七天的帖子
        List<BbsPost> posts = postMapper.listPostsInLastSevenDays();
        for (BbsPost post : posts) {
            //计算帖子的热度分
            double heatScore = postRecommendScoreService.calculateHeatScore(post);
            //todo 计算帖子的停留分
            double stayScore = postRecommendScoreService.calculateStayScore(post);
            //计算帖子的关键词分
            double keywordScore = postRecommendScoreService.calculateKeywordScore(post);
            //最终得分想加
            double finalScore = heatScore + stayScore + keywordScore;
            //进行入库操作
            PostRecommendScore postRecommendScore = new PostRecommendScore();
            postRecommendScore.setPostId(post.getId());
            postRecommendScore.setHeatScore(heatScore);
            postRecommendScore.setStayScore(stayScore);
            postRecommendScore.setKeywordScore(keywordScore);
            postRecommendScore.setFinalScore(finalScore);
            //可能插入可能修改
            postRecommendScoreService.saveOrUpdate(postRecommendScore);
            //redis zset进行一个排行按照总分
            RedisUtils.zAdd("post:recommend:final_score", post.getId(), finalScore);
        }


    }

    //获取帖子的详细信息根据id 保存到redis
    @XxlJob("PostDetailJob")
    public void getPostDetail() {
        List<String> list = RedisUtils.zTop("post:recommend:final_score", 10);
        //根据id查询帖子详细并且放入另外一个缓存
        for (String id : list) {
            BbsPost post = postMapper.getById(id);
            //放到另外一个集合中去，方便一下子取出来
            RedisUtils.setCacheList("post:detail:", List.of(post));
            //设置一天的有效期
            RedisUtils.expire("post:detail:" + id, Duration.ofDays(1));
            //todo 如果redis挂了呢？所以可以考虑入库
        }
    }
}
