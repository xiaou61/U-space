<template>
  <div class="team-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>👥 学习小组</h1>
        <p class="header-subtitle">组队学习，互相监督，共同进步</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="goToCreate">
          <el-icon><Plus /></el-icon>
          创建小组
        </el-button>
        <el-button @click="goToMy">
          <el-icon><User /></el-icon>
          我的小组
        </el-button>
      </div>
    </div>

    <!-- 主体内容区域 - 三栏布局 -->
    <div class="team-main">
      <!-- 左侧边栏 -->
      <aside class="sidebar sidebar-left">
        <!-- 类型筛选 -->
        <div class="sidebar-card filter-card">
          <div class="card-header">
            <el-icon><Filter /></el-icon>
            <span>小组类型</span>
          </div>
          <div class="filter-list">
            <div 
              class="filter-item"
              :class="{ active: filterType === null }"
              @click="filterType = null; loadTeamList()"
            >
              <span class="filter-icon">📚</span>
              <span>全部小组</span>
            </div>
            <div 
              class="filter-item"
              :class="{ active: filterType === 1 }"
              @click="filterType = 1; loadTeamList()"
            >
              <span class="filter-icon">🎯</span>
              <span>目标型</span>
            </div>
            <div 
              class="filter-item"
              :class="{ active: filterType === 2 }"
              @click="filterType = 2; loadTeamList()"
            >
              <span class="filter-icon">📖</span>
              <span>学习型</span>
            </div>
            <div 
              class="filter-item"
              :class="{ active: filterType === 3 }"
              @click="filterType = 3; loadTeamList()"
            >
              <span class="filter-icon">✅</span>
              <span>打卡型</span>
            </div>
          </div>
        </div>

        <!-- 邀请码加入 -->
        <div class="sidebar-card invite-card">
          <div class="card-header">
            <el-icon><Key /></el-icon>
            <span>邀请码加入</span>
          </div>
          <div class="invite-input">
            <el-input 
              v-model="inviteCode" 
              placeholder="输入邀请码"
              @keyup.enter="joinByCode"
            >
              <template #append>
                <el-button @click="joinByCode" :loading="joiningByCode">
                  加入
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </aside>

      <!-- 中间内容区 -->
      <main class="main-content">
        <!-- 搜索和排序 -->
        <div class="content-header-card">
          <div class="search-bar">
            <el-input 
              v-model="keyword" 
              placeholder="搜索小组名称、标签..."
              clearable
              @keyup.enter="loadTeamList"
              @clear="loadTeamList"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="loadTeamList">搜索</el-button>
          </div>
          
          <div class="content-tabs">
            <div class="tabs-left">
              <button 
                class="tab-btn" 
                :class="{ active: sortBy === 'time' }"
                @click="sortBy = 'time'; loadTeamList()"
              >
                <el-icon><Clock /></el-icon>
                最新
              </button>
              <button 
                class="tab-btn" 
                :class="{ active: sortBy === 'hot' }"
                @click="sortBy = 'hot'; loadTeamList()"
              >
                <el-icon><TrendCharts /></el-icon>
                最热
              </button>
            </div>
            <div class="tabs-right">
              <span class="teams-count">共 {{ total }} 个小组</span>
            </div>
          </div>
        </div>

        <!-- 小组列表 -->
        <div v-loading="loading" class="teams-list">
          <TeamCard 
            v-for="team in teamList" 
            :key="team.id" 
            :team="team"
            @refresh="loadTeamList"
            @apply="openApplyDialog"
          />
          
          <div v-if="!loading && teamList.length === 0" class="empty-state">
            <div class="empty-icon">📋</div>
            <p>暂无小组，快来创建第一个吧~</p>
            <el-button type="primary" @click="goToCreate">创建小组</el-button>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadTeamList"
          />
        </div>
      </main>

      <!-- 右侧边栏 -->
      <aside class="sidebar sidebar-right">
        <!-- 推荐小组 -->
        <div class="sidebar-card recommend-card">
          <div class="card-header">
            <el-icon><Star /></el-icon>
            <span>推荐小组</span>
          </div>
          <div v-loading="recommendLoading" class="recommend-list">
            <div 
              v-for="team in recommendList" 
              :key="team.id" 
              class="recommend-item"
              @click="goToDetail(team.id)"
            >
              <div class="recommend-avatar">
                <img v-if="team.teamAvatar" :src="team.teamAvatar" />
                <span v-else>{{ team.teamName?.charAt(0) || '组' }}</span>
              </div>
              <div class="recommend-info">
                <div class="recommend-name">{{ team.teamName }}</div>
                <div class="recommend-meta">
                  {{ team.currentMembers }}人 · {{ getTypeText(team.teamType) }}
                </div>
              </div>
            </div>
            <div v-if="!recommendLoading && recommendList.length === 0" class="empty-tip">
              暂无推荐
            </div>
          </div>
        </div>

        <!-- 我的小组快捷入口 -->
        <div class="sidebar-card my-teams-card" v-if="myTeamList.length > 0">
          <div class="card-header">
            <el-icon><Folder /></el-icon>
            <span>我的小组</span>
            <span class="view-all" @click="goToMy">查看全部</span>
          </div>
          <div class="my-teams-list">
            <div 
              v-for="team in myTeamList.slice(0, 3)" 
              :key="team.id" 
              class="my-team-item"
              @click="goToDetail(team.id)"
            >
              <div class="my-team-avatar">
                <img v-if="team.teamAvatar" :src="team.teamAvatar" />
                <span v-else>{{ team.teamName?.charAt(0) || '组' }}</span>
              </div>
              <span class="my-team-name">{{ team.teamName }}</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 申请加入弹窗 -->
    <el-dialog 
      v-model="showApplyDialog" 
      title="申请加入小组"
      width="400px"
    >
      <div class="apply-team-info" v-if="applyTeam">
        <div class="apply-team-name">{{ applyTeam.teamName }}</div>
        <div class="apply-team-desc">{{ applyTeam.teamDesc }}</div>
      </div>
      <el-form>
        <el-form-item label="申请理由">
          <el-input 
            v-model="applyReason" 
            type="textarea" 
            :rows="3"
            placeholder="简单介绍一下自己，为什么想加入这个小组"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitApply" :loading="applying">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Plus, User, Filter, Key, Search, Clock, TrendCharts, 
  Star, Folder 
} from '@element-plus/icons-vue'
import teamApi from '@/api/team'
import TeamCard from './components/TeamCard.vue'

const router = useRouter()

// 列表数据
const teamList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选条件
const filterType = ref(null)
const sortBy = ref('time')
const keyword = ref('')

// 邀请码
const inviteCode = ref('')
const joiningByCode = ref(false)

// 推荐小组
const recommendList = ref([])
const recommendLoading = ref(false)

// 我的小组
const myTeamList = ref([])

// 申请弹窗
const showApplyDialog = ref(false)
const applyTeam = ref(null)
const applyReason = ref('')
const applying = ref(false)

// 页面初始化
onMounted(() => {
  loadTeamList()
  loadRecommendTeams()
  loadMyTeams()
})

// 加载小组列表
const loadTeamList = async () => {
  loading.value = true
  try {
    const response = await teamApi.getTeamList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      teamType: filterType.value,
      sortBy: sortBy.value,
      keyword: keyword.value
    })
    teamList.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    console.error('加载小组列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载推荐小组
const loadRecommendTeams = async () => {
  recommendLoading.value = true
  try {
    const response = await teamApi.getRecommendTeams()
    recommendList.value = response || []
  } catch (error) {
    console.error('加载推荐小组失败:', error)
  } finally {
    recommendLoading.value = false
  }
}

// 加载我的小组
const loadMyTeams = async () => {
  try {
    const response = await teamApi.getMyTeams()
    myTeamList.value = response || []
  } catch (error) {
    console.error('加载我的小组失败:', error)
  }
}

// 通过邀请码加入
const joinByCode = async () => {
  if (!inviteCode.value.trim()) {
    ElMessage.warning('请输入邀请码')
    return
  }
  joiningByCode.value = true
  try {
    await teamApi.joinByInviteCode(inviteCode.value.trim())
    ElMessage.success('加入成功')
    inviteCode.value = ''
    loadTeamList()
    loadMyTeams()
  } catch (error) {
    console.error('加入失败:', error)
  } finally {
    joiningByCode.value = false
  }
}

// 打开申请弹窗
const openApplyDialog = (team) => {
  applyTeam.value = team
  applyReason.value = ''
  showApplyDialog.value = true
}

// 提交申请
const submitApply = async () => {
  applying.value = true
  try {
    await teamApi.applyJoin(applyTeam.value.id, { applyReason: applyReason.value })
    ElMessage.success('申请已提交，等待审核')
    showApplyDialog.value = false
    loadTeamList()
  } catch (error) {
    console.error('申请失败:', error)
  } finally {
    applying.value = false
  }
}

// 获取类型文本
const getTypeText = (type) => {
  const typeMap = { 1: '目标型', 2: '学习型', 3: '打卡型' }
  return typeMap[type] || '学习型'
}

// 路由跳转
const goToCreate = () => router.push('/team/create')
const goToMy = () => router.push('/team/my')
const goToDetail = (id) => router.push(`/team/${id}`)
</script>

<style lang="scss" scoped>
.team-container {
  padding: 24px 32px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  h1 {
    font-size: 24px;
    margin: 0 0 4px 0;
    color: #333;
  }
  
  .header-subtitle {
    font-size: 14px;
    color: #999;
    margin: 0;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
  
  @media (max-width: 600px) {
    flex-direction: column;
    gap: 16px;
    
    .header-actions {
      width: 100%;
      
      .el-button {
        flex: 1;
      }
    }
  }
}

// 三栏布局
.team-main {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 24px;
  
  @media (max-width: 1200px) {
    grid-template-columns: 200px 1fr;
    
    .sidebar-right {
      display: none;
    }
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    
    .sidebar-left {
      display: none;
    }
  }
}

// 侧边栏卡片
.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    
    .el-icon {
      color: #409eff;
    }
    
    .view-all {
      margin-left: auto;
      font-size: 12px;
      font-weight: normal;
      color: #409eff;
      cursor: pointer;
    }
  }
}

// 类型筛选
.filter-list {
  .filter-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;
    color: #666;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.active {
      background: #ecf5ff;
      color: #409eff;
      font-weight: 500;
    }
    
    .filter-icon {
      font-size: 16px;
    }
  }
}

// 邀请码输入
.invite-input {
  .el-input {
    :deep(.el-input-group__append) {
      padding: 0;
      
      .el-button {
        border: none;
        border-radius: 0 4px 4px 0;
      }
    }
  }
}

// 内容区头部
.content-header-card {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .el-input {
    flex: 1;
  }
}

.content-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .tabs-left {
    display: flex;
    gap: 8px;
  }
  
  .tab-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: none;
    background: #f5f7fa;
    border-radius: 8px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #ecf5ff;
      color: #409eff;
    }
    
    &.active {
      background: #409eff;
      color: white;
    }
  }
  
  .teams-count {
    font-size: 13px;
    color: #999;
  }
}

// 小组列表
.teams-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  p {
    color: #999;
    margin: 0 0 16px 0;
  }
}

// 分页
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

// 推荐小组列表
.recommend-list {
  min-height: 100px;
  
  .recommend-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #f5f7fa;
    }
  }
  
  .recommend-avatar {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    span {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      color: white;
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .recommend-info {
    flex: 1;
    min-width: 0;
  }
  
  .recommend-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .recommend-meta {
    font-size: 12px;
    color: #999;
    margin-top: 2px;
  }
  
  .empty-tip {
    text-align: center;
    padding: 20px;
    color: #999;
    font-size: 13px;
  }
}

// 我的小组列表
.my-teams-list {
  .my-team-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #f5f7fa;
    }
  }
  
  .my-team-avatar {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    span {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      color: white;
      font-size: 14px;
      font-weight: bold;
    }
  }
  
  .my-team-name {
    font-size: 14px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 申请弹窗
.apply-team-info {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .apply-team-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
  }
  
  .apply-team-desc {
    font-size: 14px;
    color: #666;
    line-height: 1.5;
  }
}
</style>
