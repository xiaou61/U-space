<template>
  <div class="codepen-square">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">代码广场</h1>
      <el-button 
        type="primary" 
        icon="Plus" 
        @click="createNewPen"
      >
        创建作品
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索作品标题或描述..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-bar">
        <div class="filter-item">
          <span class="filter-label">分类：</span>
          <el-radio-group v-model="filterCategory" @change="handleFilter">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="动画">动画特效</el-radio-button>
            <el-radio-button label="组件">组件库</el-radio-button>
            <el-radio-button label="游戏">游戏</el-radio-button>
            <el-radio-button label="工具">工具</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <span class="filter-label">类型：</span>
          <el-radio-group v-model="filterType" @change="handleFilter">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button :label="1">免费</el-radio-button>
            <el-radio-button :label="0">付费</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <span class="filter-label">排序：</span>
          <el-select v-model="sortBy" @change="handleFilter" style="width: 150px">
            <el-option label="最新发布" value="latest" />
            <el-option label="最热门" value="hot" />
            <el-option label="最多点赞" value="most_liked" />
            <el-option label="最多浏览" value="most_viewed" />
          </el-select>
        </div>
      </div>

      <div class="hot-tags" v-if="hotTags.length > 0">
        <span class="tags-label">热门标签：</span>
        <el-tag
          v-for="tag in hotTags"
          :key="tag.tagName"
          :type="selectedTag === tag.tagName ? 'primary' : 'info'"
          style="cursor: pointer; margin-right: 10px"
          @click="selectTag(tag.tagName)"
        >
          {{ tag.tagName }} ({{ tag.useCount }})
        </el-tag>
      </div>
    </div>

    <!-- 推荐作品 -->
    <div class="recommend-section" v-if="recommendPens.length > 0 && !searchKeyword">
      <h2 class="section-title">
        <el-icon><Star /></el-icon>
        编辑推荐
      </h2>
      <div class="pen-grid">
        <PenCard
          v-for="pen in recommendPens"
          :key="pen.id"
          :pen="pen"
          @click="viewPen(pen.id)"
        />
      </div>
    </div>

    <!-- 作品列表 -->
    <div class="pen-list-section">
      <h2 class="section-title" v-if="!searchKeyword">
        {{ filterCategory ? `${filterCategory}作品` : '全部作品' }}
      </h2>
      <h2 class="section-title" v-else>
        搜索结果（{{ total }}）
      </h2>

      <div class="pen-grid" v-loading="loading">
        <PenCard
          v-for="pen in penList"
          :key="pen.id"
          :pen="pen"
          @click="viewPen(pen.id)"
        />
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && penList.length === 0"
        description="暂无作品"
      />

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadPenList"
          @size-change="loadPenList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Star } from '@element-plus/icons-vue'
import { codepenApi } from '@/api/codepen'
import PenCard from './components/PenCard.vue'

const router = useRouter()

// 搜索和筛选
const searchKeyword = ref('')
const filterCategory = ref('')
const filterType = ref(null)
const sortBy = ref('latest')
const selectedTag = ref('')
const hotTags = ref([])

// 列表数据
const loading = ref(false)
const penList = ref([])
const recommendPens = ref([])
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 创建新作品
const createNewPen = () => {
  router.push('/codepen/editor')
}

// 查看作品
const viewPen = (id) => {
  router.push(`/codepen/${id}`)
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadPenList()
}

// 筛选
const handleFilter = () => {
  pageNum.value = 1
  selectedTag.value = ''
  loadPenList()
}

// 选择标签
const selectTag = (tagName) => {
  if (selectedTag.value === tagName) {
    selectedTag.value = ''
  } else {
    selectedTag.value = tagName
    searchKeyword.value = ''
    filterCategory.value = ''
  }
  pageNum.value = 1
  loadPenList()
}

// 加载作品列表
const loadPenList = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value
    }

    // 搜索关键词
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    // 分类筛选
    if (filterCategory.value) {
      params.category = filterCategory.value
    }

    // 类型筛选
    if (filterType.value !== null) {
      params.isFree = filterType.value
    }

    // 标签筛选
    if (selectedTag.value) {
      params.tags = [selectedTag.value]
    }

    const result = await codepenApi.getPenList(params)
    penList.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载作品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载推荐作品
const loadRecommendPens = async () => {
  try {
    recommendPens.value = await codepenApi.getRecommendPens()
  } catch (error) {
    console.error('加载推荐作品失败:', error)
  }
}

// 加载热门标签
const loadHotTags = async () => {
  try {
    hotTags.value = await codepenApi.getHotTags()
  } catch (error) {
    console.error('加载热门标签失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadPenList()
  loadRecommendPens()
  loadHotTags()
})
</script>

<style scoped lang="scss">
.codepen-square {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;

    .page-title {
      font-size: 32px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }
  }

  .filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 30px;

    .search-bar {
      margin-bottom: 20px;

      .el-input {
        max-width: 600px;
      }
    }

    .filter-bar {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      margin-bottom: 15px;

      .filter-item {
        display: flex;
        align-items: center;
        gap: 10px;

        .filter-label {
          font-size: 14px;
          color: #666;
          white-space: nowrap;
        }
      }
    }

    .hot-tags {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      padding-top: 15px;
      border-top: 1px solid #eee;

      .tags-label {
        font-size: 14px;
        color: #666;
        margin-right: 15px;
      }
    }
  }

  .recommend-section,
  .pen-list-section {
    margin-bottom: 30px;

    .section-title {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .pen-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      min-height: 200px;
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 30px;
  }
}
</style>

