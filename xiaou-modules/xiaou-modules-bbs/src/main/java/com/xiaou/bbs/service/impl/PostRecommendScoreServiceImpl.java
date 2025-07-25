package com.xiaou.bbs.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.bbs.domain.entity.BbsPost;
import com.xiaou.bbs.domain.entity.PostRecommendScore;
import com.xiaou.bbs.domain.resp.BbsPostResp;
import com.xiaou.bbs.domain.resp.BbsStudentInfoResp;
import com.xiaou.bbs.mapper.PostRecommendScoreMapper;
import com.xiaou.bbs.service.PostRecommendScoreService;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.redis.utils.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PostRecommendScoreServiceImpl extends ServiceImpl<PostRecommendScoreMapper, PostRecommendScore>
        implements PostRecommendScoreService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public double calculateHeatScore(BbsPost post) {
        //就是简单的浏览+评论+点赞
        Integer viewCount = post.getViewCount();
        Integer commentCount = post.getCommentCount();
        Integer likeCount = post.getLikeCount();
        double heatScore = viewCount * 0.3 + commentCount * 0.4 + likeCount * 0.3;
        return heatScore;
    }

    @Override
    public double calculateStayScore(BbsPost post) {
        return 0;
    }

    @Override
    public double calculateKeywordScore(BbsPost post) {
        // 简单版本：关键词权重表
        //todo 这里可以添加一个维护表 设置一下权重
        Map<String, Integer> keywordWeightMap = Map.of(
                "考研", 5,
                "校园食堂", 3,
                "篮球", 4,
                "论文", 2
        );

        String content = (post.getTitle() + " " + post.getContent()).toLowerCase();

        double score = 0;
        for (Map.Entry<String, Integer> entry : keywordWeightMap.entrySet()) {
            String keyword = entry.getKey().toLowerCase();
            int weight = entry.getValue();
            int count = countOccurrences(content, keyword);
            score += count * weight;
        }

        return score;
    }

    // 统计 keyword 在 content 中出现的次数
    private int countOccurrences(String content, String keyword) {
        int count = 0;
        int idx = 0;
        while ((idx = content.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }

    @Override
    public R<List<BbsPostResp>> recommendPost() {
        List<BbsPost> postResps = RedisUtils.getCacheList("post:detail:");
        List<BbsPostResp> convert = MapstructUtils.convert(postResps, BbsPostResp.class);
        //填充BbsStudentInfoResp
        //todo 这里需求不大所以这样查询也可以
        for (BbsPostResp post : convert) {
            BbsStudentInfoResp bbsStudentInfoResp = new BbsStudentInfoResp();
            bbsStudentInfoResp.setId(post.getUserId());
            bbsStudentInfoResp.setName(studentMapper.selectById(post.getUserId()).getName());
            bbsStudentInfoResp.setAvatar(studentMapper.selectById(post.getUserId()).getAvatar());
            post.setUserInfo(bbsStudentInfoResp);
        }
        return R.ok(convert);
    }


}




