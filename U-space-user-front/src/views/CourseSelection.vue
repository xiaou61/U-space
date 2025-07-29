<template>
  <div class="course-selection-page">
    <!-- Loading state -->
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="rect" style="height: 120px;" />
        <el-skeleton-item variant="rect" style="height: 120px; margin-top: 16px;" />
      </template>
      <template #default>
        <!-- 我的选课 -->
        <div v-if="myCourses.length > 0" class="my-courses-section">
          <h3 class="section-title">我的选课</h3>
          <div class="course-list">
            <el-card 
              v-for="course in myCourses" 
              :key="course.courseId" 
              class="course-card my-course-card"
              shadow="hover"
            >
              <div class="course-info">
                <h4 class="course-name">{{ course.courseInfo.courseName }}</h4>
                <p class="course-teacher">授课老师：{{ course.courseInfo.teacherName }}</p>
                <p class="course-time">{{ course.courseInfo.period }}</p>
                <p class="course-location">地点：{{ course.courseInfo.classroom }}</p>
              </div>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleDropCourse(course.courseId)"
                :loading="dropping === course.courseId"
              >
                退课
              </el-button>
            </el-card>
          </div>
        </div>

        <!-- 可选课程 -->
        <div class="available-courses-section">
          <h3 class="section-title">可选课程</h3>
          
          <!-- 分页器 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              small
            />
          </div>

          <el-empty v-if="courses.length === 0" description="暂无可选课程" />
          <div v-else class="course-list">
            <el-card 
              v-for="course in courses" 
              :key="course.id" 
              class="course-card available-course-card"
              shadow="hover"
            >
              <div class="course-info">
                <h4 class="course-name">{{ course.courseName }}</h4>
                <p class="course-teacher">授课老师：{{ course.teacherName }}</p>
                <p class="course-time">{{ course.period }}</p>
                <p class="course-location">地点：{{ course.classroom }}</p>
                <p class="course-capacity">
                  容量：{{ course.selectedCount }}/{{ course.capacity }}
                  <span class="remaining">(剩余 {{ course.capacity - course.selectedCount }} 名额)</span>
                </p>
              </div>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleGrabCourse(course.id)"
                :loading="grabbing === course.id"
                :disabled="course.selectedCount >= course.capacity || isAlreadySelected(course.id)"
              >
                {{ isAlreadySelected(course.id) ? '已选' : '抢课' }}
              </el-button>
            </el-card>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, grabCourse, dropCourse, getMySelectedCourses } from '../api/course'

// 响应式数据
const loading = ref(false)
const courses = ref([])
const myCourses = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const grabbing = ref('')
const dropping = ref('')

// 计算属性：检查课程是否已被选择
const isAlreadySelected = computed(() => (courseId) => {
  return myCourses.value.some(course => course.courseId === courseId)
})

// 加载课程列表
const loadCourses = async () => {
  try {
    loading.value = true
    const response = await getCourseList({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (response.code === 200) {
      courses.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.msg || '加载课程列表失败')
    }
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

// 加载我的选课
const loadMyCourses = async () => {
  try {
    const response = await getMySelectedCourses()
    if (response.code === 200) {
      myCourses.value = response.data || []
    }
  } catch (error) {
    console.error('加载我的选课失败:', error)
  }
}

// 抢课处理
const handleGrabCourse = async (courseId) => {
  try {
    await ElMessageBox.confirm('确定要抢选这门课程吗？', '确认抢课', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    grabbing.value = courseId
    const response = await grabCourse({ courseId })
    
    if (response.code === 200) {
      ElMessage.success('抢课成功！')
      // 重新加载数据
      await Promise.all([loadCourses(), loadMyCourses()])
    } else {
      ElMessage.error(response.msg || '抢课失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('抢课失败:', error)
      ElMessage.error('抢课失败')
    }
  } finally {
    grabbing.value = ''
  }
}

// 退课处理
const handleDropCourse = async (courseId) => {
  try {
    await ElMessageBox.confirm('确定要退选这门课程吗？', '确认退课', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    dropping.value = courseId
    const response = await dropCourse(courseId)
    
    if (response.code === 200) {
      ElMessage.success('退课成功！')
      // 重新加载数据
      await Promise.all([loadCourses(), loadMyCourses()])
    } else {
      ElMessage.error(response.msg || '退课失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退课失败:', error)
      ElMessage.error('退课失败')
    }
  } finally {
    dropping.value = ''
  }
}

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadCourses()
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  loadCourses()
}

// 页面初始化
onMounted(() => {
  Promise.all([loadCourses(), loadMyCourses()])
})
</script>

<style scoped>
.course-selection-page {
  padding: 16px;
  padding-bottom: 72px; /* 避免底部导航遮挡 */
}

.section-title {
  margin: 16px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
}

.my-courses-section {
  margin-bottom: 24px;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.my-course-card {
  background: linear-gradient(135deg, #e8f5e8 0%, #f0f9f0 100%);
  border-left: 4px solid #67c23a;
}

.available-course-card {
  background: linear-gradient(135deg, #f0f8ff 0%, #f8faff 100%);
  border-left: 4px solid #409eff;
}

.course-info {
  flex: 1;
}

.course-name {
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #303133;
}

.course-teacher,
.course-time,
.course-location,
.course-capacity {
  margin: 4px 0;
  font-size: 0.9rem;
  color: #606266;
}

.remaining {
  color: #e6a23c;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .course-selection-page {
    padding: 12px;
  }
  
  .course-card {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .course-card .el-button {
    align-self: center;
    width: fit-content;
  }
  
  .pagination-wrapper .el-pagination {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 1rem;
  }
  
  .course-name {
    font-size: 1rem;
  }
  
  .course-teacher,
  .course-time,
  .course-location,
  .course-capacity {
    font-size: 0.85rem;
  }
}
</style> 