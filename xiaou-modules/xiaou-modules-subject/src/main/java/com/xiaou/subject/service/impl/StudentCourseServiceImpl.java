package com.xiaou.subject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.mq.utils.RabbitMQUtils;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.subject.domain.entity.Course;
import com.xiaou.subject.domain.entity.StudentCourse;
import com.xiaou.subject.domain.mq.CourseGrabMq;
import com.xiaou.subject.domain.req.CourseGrabReq;
import com.xiaou.subject.domain.resp.CourseResp;
import com.xiaou.subject.domain.resp.StudentCourseResp;
import com.xiaou.subject.mapper.CourseMapper;
import com.xiaou.subject.mapper.StudentCourseMapper;
import com.xiaou.subject.service.StudentCourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse>
        implements StudentCourseService {

    @Resource
    private LoginHelper loginHelper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private RabbitMQUtils rabbitMQUtils;

    private static final String COURSE_CAPACITY_KEY = "course:capacity:";
    private static final String COURSE_SELECTED_KEY = "course:selected:";
    private static final String COURSE_LOCK_KEY = "course:lock:";
    private static final String STUDENT_COURSE_KEY = "student:course:";

    @Override
    public R<String> grabCourse(CourseGrabReq req) {
        String studentId = loginHelper.getCurrentAppUserId();
        String courseId = req.getCourseId();

        // 1. 检查学生是否已经选过这门课
        String studentCourseKey = STUDENT_COURSE_KEY + studentId + ":" + courseId;
        if (RedisUtils.hasKey(studentCourseKey)) {
            return R.fail("你已经选过这门课程");
        }

        // 2. 获取分布式锁
        String lockKey = COURSE_LOCK_KEY + courseId;
        RLock lock = null;
        try {
            // 尝试获取锁，最多等待500ms，锁自动释放时间5s
            lock = RedisUtils.getClient().getLock(lockKey);
            if (!lock.tryLock(500, 5000, TimeUnit.MILLISECONDS)) {
                return R.fail("系统繁忙，请稍后再试");
            }

            // 3. 检查课程是否存在
            Course course = courseMapper.selectById(courseId);
            if (course == null) {
                return R.fail("课程不存在");
            }

            // 4. 初始化Redis中的课程容量和已选人数（如果还没有的话）
            String capacityKey = COURSE_CAPACITY_KEY + courseId;
            String selectedKey = COURSE_SELECTED_KEY + courseId;

            if (!RedisUtils.hasKey(capacityKey)) {
                RedisUtils.setCacheObject(capacityKey, course.getCapacity());
            }
            if (!RedisUtils.hasKey(selectedKey)) {
                RedisUtils.setCacheObject(selectedKey, course.getSelectedCount());
            }

            // 5. 使用Redis原子操作检查是否还有名额
            RAtomicLong selectedCount = RedisUtils.getClient().getAtomicLong(selectedKey);
            RAtomicLong capacity = RedisUtils.getClient().getAtomicLong(capacityKey);

            long currentSelected = selectedCount.get();
            long maxCapacity = capacity.get();

            if (currentSelected >= maxCapacity) {
                return R.fail("课程名额已满");
            }

            // 6. 原子性增加已选人数
            long newSelectedCount = selectedCount.incrementAndGet();
            if (newSelectedCount > maxCapacity) {
                // 超出容量，回滚
                selectedCount.decrementAndGet();
                return R.fail("课程名额已满");
            }

            // 7. 标记学生已选择该课程（防止重复选课）
            RedisUtils.setCacheObject(studentCourseKey, studentId, java.time.Duration.ofHours(24));

            // 8. 发送消息到队列进行异步持久化
            CourseGrabMq mq = new CourseGrabMq();
            mq.setStudentId(studentId);
            mq.setCourseId(courseId);
            mq.setGrabTime(new Date());
            mq.setOperation(0); // 0表示选课

            // 通过 MQ 异步处理数据库持久化
            rabbitMQUtils.sendCourseGrabMessage(mq);

            log.info("学生{}成功抢课{}，当前已选人数：{}/{}", studentId, courseId, newSelectedCount, maxCapacity);
            return R.ok("抢课成功！");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("抢课过程中被中断", e);
            return R.fail("系统异常，请重试");
        } catch (Exception e) {
            log.error("抢课异常", e);
            return R.fail("系统异常，请重试");
        } finally {
            // 9. 释放锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public R<String> dropCourse(String courseId) {
        String studentId = loginHelper.getCurrentAppUserId();

        // 检查是否已选该课程
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId)
                .eq("course_id", courseId)
                .eq("status", 0);

        StudentCourse studentCourse = this.getOne(queryWrapper);
        if (studentCourse == null) {
            return R.fail("你没有选择这门课程");
        }

        String lockKey = COURSE_LOCK_KEY + courseId;
        RLock lock = null;
        try {
            lock = RedisUtils.getClient().getLock(lockKey);
            if (!lock.tryLock(500, 5000, TimeUnit.MILLISECONDS)) {
                return R.fail("系统繁忙，请稍后再试");
            }

            // 减少Redis中的已选人数
            String selectedKey = COURSE_SELECTED_KEY + courseId;
            if (RedisUtils.hasKey(selectedKey)) {
                RAtomicLong selectedCount = RedisUtils.getClient().getAtomicLong(selectedKey);
                selectedCount.decrementAndGet();
            }

            // 删除学生选课标记
            String studentCourseKey = STUDENT_COURSE_KEY + studentId + ":" + courseId;
            RedisUtils.deleteObject(studentCourseKey);

            // 发送退课消息到队列
            CourseGrabMq mq = new CourseGrabMq();
            mq.setStudentId(studentId);
            mq.setCourseId(courseId);
            mq.setGrabTime(new Date());
            mq.setOperation(1); // 1表示退课

            // 通过 MQ 异步处理数据库更新
            rabbitMQUtils.sendCourseGrabMessage(mq);

            log.info("学生{}成功退课{}", studentId, courseId);
            return R.ok("退课成功！");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("退课过程中被中断", e);
            return R.fail("系统异常，请重试");
        } catch (Exception e) {
            log.error("退课异常", e);
            return R.fail("系统异常，请重试");
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public R<List<StudentCourseResp>> getMySelectedCourses() {
        String studentId = loginHelper.getCurrentAppUserId();

        // 查询学生选课记录
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId)
                .eq("status", 0)
                .orderByDesc("select_time");

        List<StudentCourse> studentCourses = this.list(queryWrapper);

        List<StudentCourseResp> convert = MapstructUtils.convert(studentCourses, StudentCourseResp.class);
        for (StudentCourseResp course : convert) {
            Course courseInfo = courseMapper.selectById(course.getCourseId());
            course.setCourseInfo(courseInfo);
        }
        return R.ok(convert);
    }


    @Override
    @Transactional
    public void asyncProcessCourseOperation(String studentId, String courseId, Integer operation) {
        try {
            if (operation == 0) {
                // 选课操作
                StudentCourse studentCourse = new StudentCourse();
                studentCourse.setStudentId(studentId);
                studentCourse.setCourseId(courseId);
                studentCourse.setStatus(0);
                studentCourse.setSelectTime(new Date());
                studentCourse.setCreateTime(new Date());
                this.save(studentCourse);

                // 更新课程表的已选人数
                Course course = courseMapper.selectById(courseId);
                if (course != null) {
                    course.setSelectedCount(course.getSelectedCount() + 1);
                    courseMapper.updateById(course);
                }

            } else if (operation == 1) {
                // 退课操作
                QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("student_id", studentId)
                        .eq("course_id", courseId)
                        .eq("status", 0);

                StudentCourse studentCourse = this.getOne(queryWrapper);
                if (studentCourse != null) {
                    studentCourse.setStatus(1);
                    studentCourse.setCancelTime(new Date());
                    this.updateById(studentCourse);

                    // 更新课程表的已选人数
                    Course course = courseMapper.selectById(courseId);
                    if (course != null && course.getSelectedCount() > 0) {
                        course.setSelectedCount(course.getSelectedCount() - 1);
                        courseMapper.updateById(course);
                    }
                }
            }

            log.info("异步处理选课操作完成：studentId={}, courseId={}, operation={}",
                    studentId, courseId, operation);

        } catch (Exception e) {
            log.error("异步处理选课操作失败：studentId={}, courseId={}, operation={}",
                    studentId, courseId, operation, e);
        }
    }
}