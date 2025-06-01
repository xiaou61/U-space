package com.xiaou.bbs.manager;

import com.xiaou.utils.RedisUtils;
import org.redisson.api.RSet;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostLikeManager {

    public static final String LIKE_SET_KEY_PREFIX = "post:like:set:";

    /**
     * 批量查询某用户对多个帖子是否点赞
     * @param userId 当前用户ID
     * @param postIds 帖子ID列表
     * @return Map<PostId, Boolean> 点赞状态映射
     */
    public Map<Long, Boolean> getUserLikedStatus(Long userId, List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Boolean> result = new HashMap<>();
        var redisClient = RedisUtils.getClient();

        for (Long postId : postIds) {
            String likeSetKey = LIKE_SET_KEY_PREFIX + postId;
            RSet<Long> likeUserSet = redisClient.getSet(likeSetKey);
            boolean liked = likeUserSet.contains(userId);
            result.put(postId, liked);
        }
        return result;
    }

}
