<template>
  <div class="sql-detail-dialog">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="跟踪ID">
        <el-tag type="info">{{ sqlLog.traceId }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="用户信息">
        {{ sqlLog.username || '-' }} ({{ sqlLog.userType || '-' }})
      </el-descriptions-item>
      <el-descriptions-item label="请求信息">
        <el-tag :type="getMethodTagType(sqlLog.httpMethod)">
          {{ sqlLog.httpMethod }}
        </el-tag>
        {{ sqlLog.requestUri }}
      </el-descriptions-item>
      <el-descriptions-item label="客户端IP">
        {{ sqlLog.requestIp || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="模块方法">
        {{ sqlLog.module }}.{{ sqlLog.method }}
      </el-descriptions-item>
      <el-descriptions-item label="Mapper方法">
        {{ sqlLog.mapperMethod }}
      </el-descriptions-item>
      <el-descriptions-item label="SQL类型">
        <el-tag :type="getSqlTypeTagType(sqlLog.sqlType)">
          {{ sqlLog.sqlType }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="执行时间">
        <span :class="getExecutionTimeClass(sqlLog.executionTime)">
          {{ sqlLog.executionTime }}ms
        </span>
        <el-tag v-if="sqlLog.slowSql" type="warning" size="small" style="margin-left: 8px">
          慢SQL
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="影响行数">
        {{ sqlLog.affectedRows }}
      </el-descriptions-item>
      <el-descriptions-item label="执行状态">
        <el-tag :type="sqlLog.success ? 'success' : 'danger'">
          {{ sqlLog.success ? '成功' : '失败' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="执行时间">
        {{ formatDateTime(sqlLog.executeTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatDateTime(sqlLog.createTime) }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- SQL语句 -->
    <el-card class="sql-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>SQL语句</span>
          <el-button size="small" @click="copySql">
            <el-icon><DocumentCopy /></el-icon>
            复制
          </el-button>
        </div>
      </template>
      <div class="sql-content">
        <pre><code class="sql">{{ formatSql(sqlLog.sqlStatement) }}</code></pre>
      </div>
    </el-card>

    <!-- SQL参数 -->
    <el-card class="params-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>SQL参数</span>
          <el-button size="small" @click="copyParams">
            <el-icon><DocumentCopy /></el-icon>
            复制
          </el-button>
        </div>
      </template>
      <div class="params-content">
        <pre><code class="json">{{ formatParams(sqlLog.sqlParams) }}</code></pre>
      </div>
    </el-card>

    <!-- 错误信息 -->
    <el-card v-if="!sqlLog.success && sqlLog.errorMessage" class="error-card" shadow="never">
      <template #header>
        <span style="color: #F56C6C;">错误信息</span>
      </template>
      <div class="error-content">
        <pre><code class="error">{{ sqlLog.errorMessage }}</code></pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  sqlLog: {
    type: Object,
    required: true
  }
})

// 获取HTTP方法标签类型
const getMethodTagType = (method) => {
  const typeMap = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[method] || 'info'
}

// 获取SQL类型标签类型
const getSqlTypeTagType = (sqlType) => {
  const typeMap = {
    'SELECT': 'info',
    'INSERT': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[sqlType] || 'info'
}

// 获取执行时间样式类
const getExecutionTimeClass = (time) => {
  if (time > 1000) return 'slow-sql'
  if (time > 500) return 'medium-sql'
  return 'fast-sql'
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化SQL语句
const formatSql = (sql) => {
  if (!sql) return ''
  
  // 简单的SQL格式化
  return sql
    .replace(/\s+/g, ' ')
    .replace(/\b(SELECT|FROM|WHERE|JOIN|LEFT|RIGHT|INNER|ON|GROUP BY|ORDER BY|HAVING|UNION|INSERT|UPDATE|DELETE|SET|VALUES)\b/gi, '\n$1')
    .replace(/,\s+/g, ',\n  ')
    .trim()
}

// 格式化参数
const formatParams = (params) => {
  if (!params) return '[]'
  
  try {
    // 如果是JSON字符串，尝试格式化
    if (typeof params === 'string') {
      if (params.startsWith('[') || params.startsWith('{')) {
        const parsed = JSON.parse(params)
        return JSON.stringify(parsed, null, 2)
      }
      return params
    }
    return JSON.stringify(params, null, 2)
  } catch (e) {
    return params
  }
}

// 复制SQL
const copySql = async () => {
  try {
    await navigator.clipboard.writeText(props.sqlLog.sqlStatement)
    ElMessage.success('SQL语句已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 复制参数
const copyParams = async () => {
  try {
    await navigator.clipboard.writeText(props.sqlLog.sqlParams)
    ElMessage.success('SQL参数已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.sql-detail-dialog {
  max-height: 70vh;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sql-card,
.params-card,
.error-card {
  margin-top: 20px;
}

.sql-content,
.params-content,
.error-content {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.sql-content pre,
.params-content pre,
.error-content pre {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.sql-content code.sql {
  color: #0066cc;
}

.params-content code.json {
  color: #666;
}

.error-content code.error {
  color: #F56C6C;
}

.slow-sql {
  color: #F56C6C;
  font-weight: bold;
}

.medium-sql {
  color: #E6A23C;
  font-weight: bold;
}

.fast-sql {
  color: #67C23A;
}

:deep(.el-descriptions__body) {
  background-color: #fafafa;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
  color: #606266;
}
</style> 