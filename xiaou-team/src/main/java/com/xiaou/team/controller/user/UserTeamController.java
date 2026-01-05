package com.xiaou.team.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.team.dto.*;
import com.xiaou.team.service.StudyTeamService;
import com.xiaou.team.service.TeamCheckinService;
import com.xiaou.team.service.TeamDiscussionService;
import com.xiaou.team.service.TeamMemberService;
import com.xiaou.team.service.TeamRankService;
import com.xiaou.team.service.TeamStatsService;
import com.xiaou.team.service.TeamTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户端学习小组控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/team")
@RequiredArgsConstructor
public class UserTeamController {
    
    private final StudyTeamService teamService;
    private final TeamMemberService memberService;
    private final TeamTaskService taskService;
    private final TeamCheckinService checkinService;
    private final TeamRankService rankService;
    private final TeamDiscussionService discussionService;
    private final TeamStatsService statsService;
    
    /**
     * 创建小组
     */
    @PostMapping("/create")
    public Result<TeamResponse> createTeam(@RequestBody TeamCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            TeamResponse response = teamService.createTeam(userId, request);
            return Result.success("创建成功", response);
        } catch (Exception e) {
            log.error("创建小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新小组
     */
    @PutMapping("/{teamId}")
    public Result<TeamResponse> updateTeam(@PathVariable Long teamId, @RequestBody TeamCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            TeamResponse response = teamService.updateTeam(userId, teamId, request);
            return Result.success("更新成功", response);
        } catch (Exception e) {
            log.error("更新小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 解散小组
     */
    @DeleteMapping("/{teamId}")
    public Result<Boolean> dissolveTeam(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = teamService.dissolveTeam(userId, teamId);
            return success ? Result.success("解散成功", true) : Result.error("解散失败");
        } catch (Exception e) {
            log.error("解散小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组详情
     */
    @GetMapping("/{teamId}")
    public Result<TeamDetailResponse> getTeamDetail(@PathVariable Long teamId) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            TeamDetailResponse response = teamService.getTeamDetail(userId, teamId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取小组详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组列表（广场）
     */
    @PostMapping("/list")
    public Result<PageResult<TeamResponse>> getTeamList(@RequestBody TeamListRequest request) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            PageResult<TeamResponse> response = teamService.getTeamList(userId, request);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取小组列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的小组列表
     */
    @GetMapping("/my")
    public Result<List<TeamResponse>> getMyTeams() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<TeamResponse> response = teamService.getMyTeams(userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取我的小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我创建的小组列表
     */
    @GetMapping("/created")
    public Result<List<TeamResponse>> getCreatedTeams() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<TeamResponse> response = teamService.getCreatedTeams(userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取我创建的小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取推荐小组
     */
    @GetMapping("/recommend")
    public Result<List<TeamResponse>> getRecommendTeams() {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<TeamResponse> response = teamService.getRecommendTeams(userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取推荐小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取邀请码
     */
    @GetMapping("/{teamId}/invite-code")
    public Result<String> getInviteCode(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            String code = teamService.getInviteCode(userId, teamId);
            return Result.success("获取成功", code);
        } catch (Exception e) {
            log.error("获取邀请码失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 刷新邀请码
     */
    @PostMapping("/{teamId}/invite-code/refresh")
    public Result<String> refreshInviteCode(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            String code = teamService.refreshInviteCode(userId, teamId);
            return Result.success("刷新成功", code);
        } catch (Exception e) {
            log.error("刷新邀请码失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据邀请码获取小组信息
     */
    @GetMapping("/by-code/{inviteCode}")
    public Result<TeamResponse> getTeamByInviteCode(@PathVariable String inviteCode) {
        try {
            TeamResponse response = teamService.getTeamByInviteCode(inviteCode);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("根据邀请码获取小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 成员管理接口 ====================
    
    /**
     * 申请加入小组
     */
    @PostMapping("/{teamId}/join")
    public Result<Boolean> applyJoin(@PathVariable Long teamId, @RequestBody(required = false) JoinRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            if (request == null) {
                request = new JoinRequest();
            }
            request.setTeamId(teamId);
            boolean success = memberService.applyJoin(userId, request);
            return success ? Result.success("申请成功", true) : Result.error("申请失败");
        } catch (Exception e) {
            log.error("申请加入小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 通过邀请码加入小组
     */
    @PostMapping("/join-by-code")
    public Result<Boolean> joinByInviteCode(@RequestBody JoinRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.joinByInviteCode(userId, request.getInviteCode());
            return success ? Result.success("加入成功", true) : Result.error("加入失败");
        } catch (Exception e) {
            log.error("通过邀请码加入小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 退出小组
     */
    @PostMapping("/{teamId}/quit")
    public Result<Boolean> quitTeam(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.quitTeam(userId, teamId);
            return success ? Result.success("退出成功", true) : Result.error("退出失败");
        } catch (Exception e) {
            log.error("退出小组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组成员列表
     */
    @GetMapping("/{teamId}/members")
    public Result<List<MemberResponse>> getMemberList(@PathVariable Long teamId) {
        try {
            List<MemberResponse> response = memberService.getMemberList(teamId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取成员列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组申请列表
     */
    @GetMapping("/{teamId}/applications")
    public Result<List<ApplicationResponse>> getApplicationList(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<ApplicationResponse> response = memberService.getApplicationList(userId, teamId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的申请记录
     */
    @GetMapping("/applications/my")
    public Result<List<ApplicationResponse>> getMyApplications() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<ApplicationResponse> response = memberService.getMyApplications(userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取我的申请记录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审批申请 - 通过
     */
    @PostMapping("/{teamId}/application/{applicationId}/approve")
    public Result<Boolean> approveApplication(@PathVariable Long teamId, @PathVariable Long applicationId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.approveApplication(userId, applicationId);
            return success ? Result.success("审批通过", true) : Result.error("审批失败");
        } catch (Exception e) {
            log.error("审批申请失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 审批申请 - 拒绝
     */
    @PostMapping("/{teamId}/application/{applicationId}/reject")
    public Result<Boolean> rejectApplication(@PathVariable Long teamId, @PathVariable Long applicationId,
                                             @RequestParam(required = false) String rejectReason) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.rejectApplication(userId, applicationId, rejectReason);
            return success ? Result.success("已拒绝", true) : Result.error("操作失败");
        } catch (Exception e) {
            log.error("拒绝申请失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消申请
     */
    @PostMapping("/application/{applicationId}/cancel")
    public Result<Boolean> cancelApplication(@PathVariable Long applicationId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.cancelApplication(userId, applicationId);
            return success ? Result.success("取消成功", true) : Result.error("取消失败");
        } catch (Exception e) {
            log.error("取消申请失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 移除成员
     */
    @DeleteMapping("/{teamId}/member/{targetUserId}")
    public Result<Boolean> removeMember(@PathVariable Long teamId, @PathVariable Long targetUserId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.removeMember(userId, teamId, targetUserId);
            return success ? Result.success("移除成功", true) : Result.error("移除失败");
        } catch (Exception e) {
            log.error("移除成员失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设置成员角色
     */
    @PutMapping("/{teamId}/member/{targetUserId}/role")
    public Result<Boolean> setMemberRole(@PathVariable Long teamId, @PathVariable Long targetUserId,
                                         @RequestParam Integer role) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.setMemberRole(userId, teamId, targetUserId, role);
            return success ? Result.success("设置成功", true) : Result.error("设置失败");
        } catch (Exception e) {
            log.error("设置成员角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 转让组长
     */
    @PutMapping("/{teamId}/transfer")
    public Result<Boolean> transferLeader(@PathVariable Long teamId, @RequestParam Long newLeaderId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.transferLeader(userId, teamId, newLeaderId);
            return success ? Result.success("转让成功", true) : Result.error("转让失败");
        } catch (Exception e) {
            log.error("转让组长失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 禁言成员
     */
    @PostMapping("/{teamId}/member/{targetUserId}/mute")
    public Result<Boolean> muteMember(@PathVariable Long teamId, @PathVariable Long targetUserId,
                                      @RequestParam Integer minutes) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.muteMember(userId, teamId, targetUserId, minutes);
            return success ? Result.success("禁言成功", true) : Result.error("禁言失败");
        } catch (Exception e) {
            log.error("禁言成员失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 解除禁言
     */
    @DeleteMapping("/{teamId}/member/{targetUserId}/mute")
    public Result<Boolean> unmuteMember(@PathVariable Long teamId, @PathVariable Long targetUserId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = memberService.unmuteMember(userId, teamId, targetUserId);
            return success ? Result.success("解除禁言成功", true) : Result.error("解除禁言失败");
        } catch (Exception e) {
            log.error("解除禁言失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 任务管理接口 ====================
    
    /**
     * 创建打卡任务
     */
    @PostMapping("/{teamId}/task")
    public Result<Long> createTask(@PathVariable Long teamId, @RequestBody TaskCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            Long taskId = taskService.createTask(teamId, request, userId);
            return Result.success("创建成功", taskId);
        } catch (Exception e) {
            log.error("创建任务失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新打卡任务
     */
    @PutMapping("/task/{taskId}")
    public Result<Boolean> updateTask(@PathVariable Long taskId, @RequestBody TaskCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            taskService.updateTask(taskId, request, userId);
            return Result.success("更新成功", true);
        } catch (Exception e) {
            log.error("更新任务失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除打卡任务
     */
    @DeleteMapping("/task/{taskId}")
    public Result<Boolean> deleteTask(@PathVariable Long taskId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            taskService.deleteTask(taskId, userId);
            return Result.success("删除成功", true);
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 启用/禁用任务
     */
    @PutMapping("/task/{taskId}/status")
    public Result<Boolean> setTaskStatus(@PathVariable Long taskId, @RequestParam Integer status) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            taskService.setTaskStatus(taskId, status, userId);
            return Result.success("操作成功", true);
        } catch (Exception e) {
            log.error("设置任务状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<TaskResponse> getTaskDetail(@PathVariable Long taskId) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            TaskResponse response = taskService.getTaskDetail(taskId, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取任务详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组任务列表
     */
    @GetMapping("/{teamId}/tasks")
    public Result<List<TaskResponse>> getTaskList(@PathVariable Long teamId,
                                                  @RequestParam(required = false) Integer status) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<TaskResponse> response = taskService.getTaskList(teamId, status, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取今日需要打卡的任务
     */
    @GetMapping("/{teamId}/tasks/today")
    public Result<List<TaskResponse>> getTodayTasks(@PathVariable Long teamId) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<TaskResponse> response = taskService.getTodayTasks(teamId, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取今日任务失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 打卡接口 ====================
    
    /**
     * 打卡
     */
    @PostMapping("/{teamId}/checkin")
    public Result<Long> checkin(@PathVariable Long teamId, @RequestBody CheckinRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            Long checkinId = checkinService.checkin(teamId, request, userId);
            return Result.success("打卡成功", checkinId);
        } catch (Exception e) {
            log.error("打卡失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 补卡
     */
    @PostMapping("/{teamId}/checkin/supplement")
    public Result<Long> supplementCheckin(@PathVariable Long teamId,
                                          @RequestBody CheckinRequest request,
                                          @RequestParam String date) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            LocalDate checkinDate = LocalDate.parse(date);
            Long checkinId = checkinService.supplementCheckin(teamId, request, checkinDate, userId);
            return Result.success("补卡成功", checkinId);
        } catch (Exception e) {
            log.error("补卡失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除打卡记录
     */
    @DeleteMapping("/checkin/{checkinId}")
    public Result<Boolean> deleteCheckin(@PathVariable Long checkinId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            checkinService.deleteCheckin(checkinId, userId);
            return Result.success("删除成功", true);
        } catch (Exception e) {
            log.error("删除打卡记录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取打卡详情
     */
    @GetMapping("/checkin/{checkinId}")
    public Result<CheckinResponse> getCheckinDetail(@PathVariable Long checkinId) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            CheckinResponse response = checkinService.getCheckinDetail(checkinId, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取打卡详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组打卡动态列表
     */
    @GetMapping("/{teamId}/checkins")
    public Result<List<CheckinResponse>> getCheckinList(@PathVariable Long teamId,
                                                        @RequestParam(required = false) Long taskId,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<CheckinResponse> response = checkinService.getCheckinList(teamId, taskId, page, pageSize, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取打卡列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的打卡记录
     */
    @GetMapping("/{teamId}/checkins/my")
    public Result<List<CheckinResponse>> getMyCheckins(@PathVariable Long teamId,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            List<CheckinResponse> response = checkinService.getUserCheckins(userId, teamId, start, end);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取我的打卡记录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取打卡日历数据
     */
    @GetMapping("/{teamId}/checkin/calendar")
    public Result<List<LocalDate>> getCheckinCalendar(@PathVariable Long teamId,
                                                      @RequestParam Integer year,
                                                      @RequestParam Integer month) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<LocalDate> response = checkinService.getCheckinCalendar(userId, teamId, year, month);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取打卡日历失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 点赞打卡
     */
    @PostMapping("/checkin/{checkinId}/like")
    public Result<Boolean> likeCheckin(@PathVariable Long checkinId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            checkinService.likeCheckin(checkinId, userId);
            return Result.success("点赞成功", true);
        } catch (Exception e) {
            log.error("点赞失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/checkin/{checkinId}/like")
    public Result<Boolean> unlikeCheckin(@PathVariable Long checkinId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            checkinService.unlikeCheckin(checkinId, userId);
            return Result.success("取消点赞成功", true);
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取连续打卡天数
     */
    @GetMapping("/{teamId}/checkin/streak")
    public Result<Integer> getStreakDays(@PathVariable Long teamId,
                                         @RequestParam(required = false) Long taskId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            Integer streak = checkinService.getStreakDays(userId, teamId, taskId);
            return Result.success("获取成功", streak);
        } catch (Exception e) {
            log.error("获取连续打卡天数失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取总打卡天数
     */
    @GetMapping("/{teamId}/checkin/total")
    public Result<Integer> getTotalCheckinDays(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            Integer total = checkinService.getTotalCheckinDays(userId, teamId);
            return Result.success("获取成功", total);
        } catch (Exception e) {
            log.error("获取总打卡天数失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 排行榜接口 ====================
    
    /**
     * 获取打卡次数排行榜
     */
    @GetMapping("/{teamId}/rank/checkin")
    public Result<List<RankResponse>> getCheckinRank(@PathVariable Long teamId,
                                                     @RequestParam(defaultValue = "total") String type,
                                                     @RequestParam(defaultValue = "20") Integer limit) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<RankResponse> response = rankService.getCheckinRank(teamId, type, limit, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取打卡排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取连续打卡排行榜
     */
    @GetMapping("/{teamId}/rank/streak")
    public Result<List<RankResponse>> getStreakRank(@PathVariable Long teamId,
                                                    @RequestParam(defaultValue = "20") Integer limit) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<RankResponse> response = rankService.getStreakRank(teamId, limit, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取连续打卡排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取学习时长排行榜
     */
    @GetMapping("/{teamId}/rank/duration")
    public Result<List<RankResponse>> getDurationRank(@PathVariable Long teamId,
                                                      @RequestParam(defaultValue = "total") String type,
                                                      @RequestParam(defaultValue = "20") Integer limit) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<RankResponse> response = rankService.getDurationRank(teamId, type, limit, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取学习时长排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取贡献值排行榜
     */
    @GetMapping("/{teamId}/rank/contribution")
    public Result<List<RankResponse>> getContributionRank(@PathVariable Long teamId,
                                                          @RequestParam(defaultValue = "20") Integer limit) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<RankResponse> response = rankService.getContributionRank(teamId, limit, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取贡献值排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的排名
     */
    @GetMapping("/{teamId}/rank/my")
    public Result<RankResponse> getMyRank(@PathVariable Long teamId,
                                          @RequestParam(defaultValue = "checkin") String rankType) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            RankResponse response = rankService.getUserRank(teamId, userId, rankType);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取我的排名失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 讨论接口 ====================
    
    /**
     * 创建讨论
     */
    @PostMapping("/{teamId}/discussion")
    public Result<Long> createDiscussion(@PathVariable Long teamId, @RequestBody DiscussionCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            Long discussionId = discussionService.createDiscussion(teamId, request, userId);
            return Result.success("发布成功", discussionId);
        } catch (Exception e) {
            log.error("创建讨论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新讨论
     */
    @PutMapping("/discussion/{discussionId}")
    public Result<Boolean> updateDiscussion(@PathVariable Long discussionId, @RequestBody DiscussionCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            discussionService.updateDiscussion(discussionId, request, userId);
            return Result.success("更新成功", true);
        } catch (Exception e) {
            log.error("更新讨论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除讨论
     */
    @DeleteMapping("/discussion/{discussionId}")
    public Result<Boolean> deleteDiscussion(@PathVariable Long discussionId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            discussionService.deleteDiscussion(discussionId, userId);
            return Result.success("删除成功", true);
        } catch (Exception e) {
            log.error("删除讨论失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取讨论详情
     */
    @GetMapping("/discussion/{discussionId}")
    public Result<DiscussionResponse> getDiscussionDetail(@PathVariable Long discussionId) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            DiscussionResponse response = discussionService.getDiscussionDetail(discussionId, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取讨论详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取讨论列表
     */
    @GetMapping("/{teamId}/discussions")
    public Result<List<DiscussionResponse>> getDiscussionList(@PathVariable Long teamId,
                                                              @RequestParam(required = false) Integer category,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            List<DiscussionResponse> response = discussionService.getDiscussionList(teamId, category, keyword, page, pageSize, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取讨论列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 置顶/取消置顶
     */
    @PutMapping("/discussion/{discussionId}/top")
    public Result<Boolean> setDiscussionTop(@PathVariable Long discussionId, @RequestParam Integer isTop) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            discussionService.setTop(discussionId, isTop, userId);
            return Result.success("操作成功", true);
        } catch (Exception e) {
            log.error("设置置顶失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设为精华/取消精华
     */
    @PutMapping("/discussion/{discussionId}/essence")
    public Result<Boolean> setDiscussionEssence(@PathVariable Long discussionId, @RequestParam Integer isEssence) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            discussionService.setEssence(discussionId, isEssence, userId);
            return Result.success("操作成功", true);
        } catch (Exception e) {
            log.error("设置精华失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 点赞讨论
     */
    @PostMapping("/discussion/{discussionId}/like")
    public Result<Boolean> likeDiscussion(@PathVariable Long discussionId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            discussionService.likeDiscussion(discussionId, userId);
            return Result.success("点赞成功", true);
        } catch (Exception e) {
            log.error("点赞失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消点赞讨论
     */
    @DeleteMapping("/discussion/{discussionId}/like")
    public Result<Boolean> unlikeDiscussion(@PathVariable Long discussionId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            discussionService.unlikeDiscussion(discussionId, userId);
            return Result.success("取消点赞成功", true);
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 统计接口 ====================
    
    /**
     * 获取小组统计概览
     */
    @GetMapping("/{teamId}/stats")
    public Result<TeamStatsResponse> getTeamStats(@PathVariable Long teamId) {
        try {
            Long userId = null;
            if (StpUserUtil.isLogin()) {
                userId = StpUserUtil.getLoginIdAsLong();
            }
            TeamStatsResponse response = statsService.getTeamStats(teamId, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取小组统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组每周统计
     */
    @GetMapping("/{teamId}/stats/weekly")
    public Result<TeamStatsResponse> getWeeklyStats(@PathVariable Long teamId) {
        try {
            TeamStatsResponse response = statsService.getWeeklyStats(teamId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取每周统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取小组每月统计
     */
    @GetMapping("/{teamId}/stats/monthly")
    public Result<TeamStatsResponse> getMonthlyStats(@PathVariable Long teamId,
                                                     @RequestParam Integer year,
                                                     @RequestParam Integer month) {
        try {
            TeamStatsResponse response = statsService.getMonthlyStats(teamId, year, month);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取每月统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户个人统计
     */
    @GetMapping("/{teamId}/stats/my")
    public Result<TeamStatsResponse.UserStats> getMyStats(@PathVariable Long teamId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            TeamStatsResponse.UserStats response = statsService.getUserStats(teamId, userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取个人统计失败", e);
            return Result.error(e.getMessage());
        }
    }
}
