package com.xiaou.bbs.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.bbs.domain.entity.BbsPost;
import com.xiaou.bbs.domain.entity.PostRecommendScore;
import com.xiaou.bbs.domain.resp.BbsPostResp;
import com.xiaou.common.domain.R;

import java.util.List;

public interface PostRecommendScoreService extends IService<PostRecommendScore> {


     double calculateHeatScore(BbsPost post);

    double calculateStayScore(BbsPost post);

    double calculateKeywordScore(BbsPost post);

    R<List<BbsPostResp>> recommendPost();
}
