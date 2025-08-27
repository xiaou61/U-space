package com.xiaou.activity.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.activity.domain.entity.VoteActivity;
import com.xiaou.activity.domain.entity.VoteOption;
import com.xiaou.activity.domain.entity.VoteRecord;
import com.xiaou.activity.domain.resp.VoteActivityResp;
import com.xiaou.activity.service.VoteActivityService;
import com.xiaou.activity.service.VoteOptionService;
import com.xiaou.activity.service.VoteRecordService;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/vote")
public class VoteController {

    @Resource
    private VoteActivityService voteActivityService;
    
    @Resource
    private VoteRecordService voteRecordService;
    
    @Resource
    private VoteOptionService voteOptionService;
    
    @Resource
    private LoginHelper loginHelper;

    /**
     * 获取所有有效的投票活动（未下架且在时间范围内）
     */
    @GetMapping("/activities")
    public R<List<VoteActivityResp>> getActiveActivities() {
        QueryWrapper<VoteActivity> queryWrapper = new QueryWrapper<>();
        Date now = new Date();
        
        // 查询条件：状态为1（进行中）且当前时间在活动时间范围内
        queryWrapper.eq("status", 1)
                   .le("start_time", now)  // 开始时间 <= 当前时间
                   .ge("end_time", now)    // 结束时间 >= 当前时间
                   .orderByDesc("create_at");
        
        List<VoteActivity> activities = voteActivityService.list(queryWrapper);
        
        // 转换为响应对象并填充选项信息
        List<VoteActivityResp> respList = activities.stream().map(activity -> {
            VoteActivityResp resp = new VoteActivityResp();
            resp.setId(activity.getId());
            resp.setTitle(activity.getTitle());
            resp.setDescription(activity.getDescription());
            resp.setStartTime(activity.getStartTime());
            resp.setEndTime(activity.getEndTime());
            resp.setStatus(activity.getStatus());
            resp.setCreateAt(activity.getCreateAt());
            resp.setUpdateAt(activity.getUpdateAt());
            
            // 获取活动的选项
            QueryWrapper<VoteOption> optionWrapper = new QueryWrapper<>();
            optionWrapper.eq("activity_id", activity.getId())
                       .orderByAsc("create_at");
            List<VoteOption> options = voteOptionService.list(optionWrapper);
            resp.setOptions(options);
            
            return resp;
        }).toList();
        
        return R.ok("获取成功", respList);
    }

    /**
     * 用户投票
     * @param activityId 活动ID
     * @param optionId 选项ID
     */
    @PostMapping("/submit")
    @Transactional
    public R<String> submitVote(@RequestParam String activityId, @RequestParam String optionId) {
        // 获取当前登录用户ID
        String userId = loginHelper.getCurrentAppUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        
        // 1. 检查活动是否存在且有效
        VoteActivity activity = voteActivityService.getById(activityId);
        if (activity == null) {
            return R.fail("投票活动不存在");
        }
        
        Date now = new Date();
        if (activity.getStatus() != 1) {
            return R.fail("该活动已下架");
        }
        
        if (now.before(activity.getStartTime())) {
            return R.fail("活动尚未开始");
        }
        
        if (now.after(activity.getEndTime())) {
            return R.fail("活动已结束");
        }
        
        // 2. 检查选项是否存在
        VoteOption option = voteOptionService.getById(optionId);
        if (option == null || !option.getActivityId().equals(activityId)) {
            return R.fail("投票选项不存在");
        }
        
        // 3. 检查用户是否已经投过票
        QueryWrapper<VoteRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("activity_id", activityId)
                    .eq("user_id", userId);
        
        VoteRecord existingRecord = voteRecordService.getOne(recordWrapper);
        if (existingRecord != null) {
            return R.fail("您已经参与过该活动的投票");
        }
        
        // 4. 保存投票记录
        VoteRecord voteRecord = new VoteRecord();
        voteRecord.setActivityId(activityId);
        voteRecord.setOptionId(optionId);
        voteRecord.setUserId(userId);
        
        boolean saveResult = voteRecordService.save(voteRecord);
        if (!saveResult) {
            return R.fail("投票失败，请重试");
        }
        
        // 5. 更新选项票数
        option.setVoteCount((option.getVoteCount() == null ? 0 : option.getVoteCount()) + 1);
        boolean updateResult = voteOptionService.updateById(option);
        if (!updateResult) {
            return R.fail("投票失败，请重试");
        }
        
        return R.ok("投票成功");
    }

    /**
     * 检查用户是否已投票
     * @param activityId 活动ID
     */
    @GetMapping("/check/{activityId}")
    public R<Boolean> checkUserVoted(@PathVariable String activityId) {
        String userId = loginHelper.getCurrentAppUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        
        QueryWrapper<VoteRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId)
              .eq("user_id", userId);
        
        boolean hasVoted = voteRecordService.count(wrapper) > 0;
        return R.ok("查询成功", hasVoted);
    }
}
