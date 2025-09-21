<template>
  <div class="text-diff">
    <!-- 头部导航 -->
    <div class="tool-header">
      <div class="header-content">
        <h1 class="tool-title">
          <el-icon><Operation /></el-icon>
          文本差异比对工具
        </h1>
        <p class="tool-description">智能文本差异对比，支持多种对比模式和结果导出</p>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button 
            type="primary" 
            @click="performDiff"
            :disabled="!leftText.trim() || !rightText.trim()"
          >
            <el-icon><Search /></el-icon>
            开始比对
          </el-button>
          <el-button @click="clearAll">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </el-button-group>
        
        <el-select v-model="diffMode" placeholder="比对模式" style="width: 120px;">
          <el-option label="字符级" value="chars" />
          <el-option label="单词级" value="words" />
          <el-option label="行级" value="lines" />
        </el-select>
      </div>
      
      <div class="toolbar-right">
        <el-checkbox v-model="ignoreWhitespace">忽略空白</el-checkbox>
        <el-checkbox v-model="ignoreCase">忽略大小写</el-checkbox>
        <el-button @click="exportDiff" :disabled="!hasDiff">
          <el-icon><Download /></el-icon>
          导出结果
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-area">
      <!-- 文本输入区域 -->
      <div class="input-panels">
        <div class="input-panel">
          <div class="panel-header">
            <h3>原文本 (左侧)</h3>
            <div class="panel-actions">
              <el-upload
                ref="leftUploadRef"
                :show-file-list="false"
                :before-upload="(file) => handleFileUpload(file, 'left')"
                accept=".txt,.md,.js,.css,.html,.json,.xml"
              >
                <el-button size="small" text>
                  <el-icon><Upload /></el-icon>
                  上传文件
                </el-button>
              </el-upload>
              <el-button size="small" text @click="pasteFromClipboard('left')">
                <el-icon><CopyDocument /></el-icon>
                粘贴
              </el-button>
            </div>
          </div>
          <div class="editor-container">
            <textarea
              v-model="leftText"
              class="text-editor"
              placeholder="请输入或粘贴原始文本..."
              @input="onTextChange"
              @scroll="syncScroll($event, 'left')"
              ref="leftEditor"
            ></textarea>
            <div class="editor-info">
              <span class="line-count">{{ getLineCount(leftText) }} 行</span>
              <span class="char-count">{{ leftText.length }} 字符</span>
            </div>
          </div>
        </div>

        <div class="input-panel">
          <div class="panel-header">
            <h3>新文本 (右侧)</h3>
            <div class="panel-actions">
              <el-upload
                ref="rightUploadRef"
                :show-file-list="false"
                :before-upload="(file) => handleFileUpload(file, 'right')"
                accept=".txt,.md,.js,.css,.html,.json,.xml"
              >
                <el-button size="small" text>
                  <el-icon><Upload /></el-icon>
                  上传文件
                </el-button>
              </el-upload>
              <el-button size="small" text @click="pasteFromClipboard('right')">
                <el-icon><CopyDocument /></el-icon>
                粘贴
              </el-button>
            </div>
          </div>
          <div class="editor-container">
            <textarea
              v-model="rightText"
              class="text-editor"
              placeholder="请输入或粘贴新文本..."
              @input="onTextChange"
              @scroll="syncScroll($event, 'right')"
              ref="rightEditor"
            ></textarea>
            <div class="editor-info">
              <span class="line-count">{{ getLineCount(rightText) }} 行</span>
              <span class="char-count">{{ rightText.length }} 字符</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 比对结果区域 -->
      <div v-if="hasDiff" class="diff-result">
        <div class="result-header">
          <h3>比对结果</h3>
          <div class="result-stats">
            <span class="stat-item added">
              <el-icon><Plus /></el-icon>
              +{{ diffStats.added }}
            </span>
            <span class="stat-item removed">
              <el-icon><Minus /></el-icon>
              -{{ diffStats.removed }}
            </span>
            <span class="stat-item modified">
              <el-icon><Edit /></el-icon>
              ~{{ diffStats.modified }}
            </span>
          </div>
          <div class="view-controls">
            <el-button-group>
              <el-button 
                :type="viewMode === 'side-by-side' ? 'primary' : ''"
                @click="viewMode = 'side-by-side'"
                size="small"
              >
                并排视图
              </el-button>
              <el-button 
                :type="viewMode === 'unified' ? 'primary' : ''"
                @click="viewMode = 'unified'"
                size="small"
              >
                统一视图
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 并排视图 -->
        <div v-if="viewMode === 'side-by-side'" class="side-by-side-view">
          <div class="diff-panel">
            <div class="diff-content" v-html="formattedLeftDiff"></div>
          </div>
          <div class="diff-panel">
            <div class="diff-content" v-html="formattedRightDiff"></div>
          </div>
        </div>

        <!-- 统一视图 -->
        <div v-else class="unified-view">
          <div class="diff-content" v-html="formattedUnifiedDiff"></div>
        </div>

        <!-- 差异导航 -->
        <div v-if="diffPositions.length > 0" class="diff-navigation">
          <el-button 
            size="small" 
            @click="previousDiff"
            :disabled="currentDiffIndex <= 0"
          >
            <el-icon><ArrowUp /></el-icon>
            上一个差异
          </el-button>
          <span class="nav-info">
            {{ currentDiffIndex + 1 }} / {{ diffPositions.length }}
          </span>
          <el-button 
            size="small" 
            @click="nextDiff"
            :disabled="currentDiffIndex >= diffPositions.length - 1"
          >
            <el-icon><ArrowDown /></el-icon>
            下一个差异
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-icon class="empty-icon"><Operation /></el-icon>
        <p class="empty-text">请在左右两个文本框中输入内容，然后点击"开始比对"</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Operation, Search, Delete, Upload, CopyDocument, Download,
  Plus, Minus, Edit, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'

// 响应式数据
const leftText = ref('')
const rightText = ref('')
const diffMode = ref('lines')
const ignoreWhitespace = ref(false)
const ignoreCase = ref(false)
const viewMode = ref('side-by-side')
const diffResult = ref(null)
const currentDiffIndex = ref(0)
const leftEditor = ref()
const rightEditor = ref()
const leftUploadRef = ref()
const rightUploadRef = ref()

// 计算属性
const hasDiff = computed(() => diffResult.value !== null)

const diffStats = computed(() => {
  if (!diffResult.value) return { added: 0, removed: 0, modified: 0 }
  
  let added = 0, removed = 0, modified = 0
  diffResult.value.forEach(part => {
    if (part.added) added++
    else if (part.removed) removed++
    else if (part.modified) modified++
  })
  
  return { added, removed, modified }
})

const diffPositions = computed(() => {
  if (!diffResult.value) return []
  return diffResult.value
    .map((part, index) => ({ ...part, index }))
    .filter(part => part.added || part.removed || part.modified)
})

const formattedLeftDiff = computed(() => {
  if (!diffResult.value) return ''
  return formatDiffForSideBySide(diffResult.value, 'left')
})

const formattedRightDiff = computed(() => {
  if (!diffResult.value) return ''
  return formatDiffForSideBySide(diffResult.value, 'right')
})

const formattedUnifiedDiff = computed(() => {
  if (!diffResult.value) return ''
  return formatDiffForUnified(diffResult.value)
})

// 执行文本比对
const performDiff = () => {
  if (!leftText.value.trim() || !rightText.value.trim()) {
    ElMessage.warning('请输入要比对的文本')
    return
  }

  try {
    const leftContent = processText(leftText.value)
    const rightContent = processText(rightText.value)
    
    diffResult.value = calculateDiff(leftContent, rightContent)
    currentDiffIndex.value = 0
    
    ElMessage.success('文本比对完成')
  } catch (error) {
    ElMessage.error('文本比对失败')
    console.error(error)
  }
}

// 处理文本（根据选项）
const processText = (text) => {
  let processed = text
  
  if (ignoreWhitespace.value) {
    processed = processed.replace(/\s+/g, ' ').trim()
  }
  
  if (ignoreCase.value) {
    processed = processed.toLowerCase()
  }
  
  return processed
}

// 计算文本差异（使用LCS算法实现精准对比）
const calculateDiff = (left, right) => {
  const leftItems = splitText(left)
  const rightItems = splitText(right)
  
  if (diffMode.value === 'chars') {
    return calculateCharacterDiff(leftItems, rightItems)
  } else {
    return calculateLineDiff(leftItems, rightItems)
  }
}

// 字符级精准差异算法
const calculateCharacterDiff = (leftChars, rightChars) => {
  const lcs = computeLCS(leftChars, rightChars)
  const diff = []
  
  let i = 0, j = 0, k = 0
  
  while (i < leftChars.length || j < rightChars.length) {
    if (k < lcs.length && i < leftChars.length && leftChars[i] === lcs[k]) {
      // 相同字符
      diff.push({ value: leftChars[i], equal: true })
      i++
      j++
      k++
    } else if (k < lcs.length && j < rightChars.length && rightChars[j] === lcs[k]) {
      // 右侧有新增字符
      diff.push({ value: rightChars[j], added: true })
      j++
    } else if (i < leftChars.length) {
      // 左侧有删除字符
      diff.push({ value: leftChars[i], removed: true })
      i++
    } else if (j < rightChars.length) {
      // 右侧有新增字符
      diff.push({ value: rightChars[j], added: true })
      j++
    }
  }
  
  // 合并相邻的相同类型字符，提高可读性
  return mergeAdjacentDiffs(diff)
}

// 合并相邻的相同类型差异
const mergeAdjacentDiffs = (diff) => {
  if (diff.length === 0) return diff
  
  const merged = []
  let current = { ...diff[0] }
  
  for (let i = 1; i < diff.length; i++) {
    const item = diff[i]
    
    // 如果类型相同，则合并
    if ((current.equal && item.equal) || 
        (current.added && item.added) || 
        (current.removed && item.removed)) {
      current.value += item.value
    } else {
      merged.push(current)
      current = { ...item }
    }
  }
  
  merged.push(current)
  return merged
}

// 行级/单词级差异算法
const calculateLineDiff = (leftItems, rightItems) => {
  const diff = []
  const maxLength = Math.max(leftItems.length, rightItems.length)
  
  for (let i = 0; i < maxLength; i++) {
    const leftItem = leftItems[i] || ''
    const rightItem = rightItems[i] || ''
    
    if (leftItem === rightItem) {
      if (leftItem || rightItem) {
        diff.push({ value: leftItem, equal: true })
      }
    } else if (!leftItem) {
      diff.push({ value: rightItem, added: true })
    } else if (!rightItem) {
      diff.push({ value: leftItem, removed: true })
    } else {
      diff.push({ value: leftItem, removed: true })
      diff.push({ value: rightItem, added: true })
    }
  }
  
  return diff
}

// 计算最长公共子序列（LCS）
const computeLCS = (arr1, arr2) => {
  const m = arr1.length
  const n = arr2.length
  const dp = Array(m + 1).fill().map(() => Array(n + 1).fill(0))
  
  // 填充DP表
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (arr1[i - 1] === arr2[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1] + 1
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1])
      }
    }
  }
  
  // 回溯构建LCS
  const lcs = []
  let i = m, j = n
  
  while (i > 0 && j > 0) {
    if (arr1[i - 1] === arr2[j - 1]) {
      lcs.unshift(arr1[i - 1])
      i--
      j--
    } else if (dp[i - 1][j] > dp[i][j - 1]) {
      i--
    } else {
      j--
    }
  }
  
  return lcs
}

// 根据模式分割文本
const splitText = (text) => {
  switch (diffMode.value) {
    case 'chars':
      return text.split('')
    case 'words':
      return text.split(/\s+/)
    case 'lines':
    default:
      return text.split('\n')
  }
}

// 格式化并排视图
const formatDiffForSideBySide = (diff, side) => {
  if (diffMode.value === 'chars') {
    // 字符级差异使用内联显示
    const content = diff.map((part, index) => {
      let className = ''
      
      if (part.added && side === 'right') {
        className = 'diff-added'
      } else if (part.removed && side === 'left') {
        className = 'diff-removed'
      } else if (part.equal) {
        className = 'diff-equal'
      } else {
        return '' // 不显示不属于当前侧的内容
      }
      
      return `<span class="${className}" data-index="${index}">${escapeHtml(part.value)}</span>`
    }).join('')
    
    return `<div class="diff-line">${content}</div>`
  } else {
    // 行级/单词级差异使用块级显示
    return diff.map((part, index) => {
      let className = ''
      let content = part.value
      
      if (part.added && side === 'right') {
        className = 'diff-added'
      } else if (part.removed && side === 'left') {
        className = 'diff-removed'
      } else if (part.equal) {
        className = 'diff-equal'
      } else {
        return '' // 不显示不属于当前侧的内容
      }
      
      return `<div class="diff-line ${className}" data-index="${index}">${escapeHtml(content)}</div>`
    }).join('')
  }
}

// 格式化统一视图
const formatDiffForUnified = (diff) => {
  if (diffMode.value === 'chars') {
    // 字符级差异使用内联显示
    const content = diff.map((part, index) => {
      let className = ''
      
      if (part.added) {
        className = 'diff-added'
      } else if (part.removed) {
        className = 'diff-removed'
      } else {
        className = 'diff-equal'
      }
      
      return `<span class="${className}" data-index="${index}">${escapeHtml(part.value)}</span>`
    }).join('')
    
    return `<div class="diff-line">${content}</div>`
  } else {
    // 行级/单词级差异使用块级显示
    return diff.map((part, index) => {
      let className = ''
      let prefix = ' '
      
      if (part.added) {
        className = 'diff-added'
        prefix = '+'
      } else if (part.removed) {
        className = 'diff-removed'
        prefix = '-'
      } else {
        className = 'diff-equal'
      }
      
      return `<div class="diff-line ${className}" data-index="${index}"><span class="diff-prefix">${prefix}</span>${escapeHtml(part.value)}</div>`
    }).join('')
  }
}

// HTML 转义
const escapeHtml = (text) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 清空所有内容
const clearAll = () => {
  leftText.value = ''
  rightText.value = ''
  diffResult.value = null
  currentDiffIndex.value = 0
}

// 从剪贴板粘贴
const pasteFromClipboard = async (side) => {
  try {
    const text = await navigator.clipboard.readText()
    if (side === 'left') {
      leftText.value = text
    } else {
      rightText.value = text
    }
    ElMessage.success('已从剪贴板粘贴')
  } catch (error) {
    ElMessage.error('粘贴失败')
  }
}

// 文件上传处理
const handleFileUpload = (file, side) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    if (side === 'left') {
      leftText.value = e.target.result
    } else {
      rightText.value = e.target.result
    }
    ElMessage.success('文件上传成功')
  }
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  reader.readAsText(file)
  return false // 阻止自动上传
}

// 导出比对结果
const exportDiff = () => {
  if (!diffResult.value) return
  
  const content = generateExportContent()
  const blob = new Blob([content], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'text-diff-result.html'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('结果导出成功')
}

// 生成导出内容
const generateExportContent = () => {
  const stats = diffStats.value
  const timestamp = new Date().toLocaleString('zh-CN')
  
  return `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>文本比对结果</title>
  <style>
    body { font-family: monospace; margin: 20px; }
    .header { margin-bottom: 20px; }
    .stats { margin: 10px 0; }
    .diff-added { background: #d4edda; color: #155724; }
    .diff-removed { background: #f8d7da; color: #721c24; }
    .diff-equal { background: #f8f9fa; }
    .diff-line { padding: 2px 5px; margin: 1px 0; }
    .diff-prefix { margin-right: 5px; font-weight: bold; }
  </style>
</head>
<body>
  <div class="header">
    <h1>文本比对结果</h1>
    <p>生成时间: ${timestamp}</p>
    <div class="stats">
      <span>新增: ${stats.added} | 删除: ${stats.removed} | 修改: ${stats.modified}</span>
    </div>
  </div>
  <div class="content">
    ${formattedUnifiedDiff.value}
  </div>
</body>
</html>`
}

// 文本变化处理
const onTextChange = () => {
  diffResult.value = null
  currentDiffIndex.value = 0
}

// 滚动同步
const syncScroll = (event, side) => {
  if (!leftEditor.value || !rightEditor.value) return
  
  const source = event.target
  const target = side === 'left' ? rightEditor.value : leftEditor.value
  
  const scrollPercentage = source.scrollTop / (source.scrollHeight - source.clientHeight)
  target.scrollTop = scrollPercentage * (target.scrollHeight - target.clientHeight)
}

// 差异导航
const previousDiff = () => {
  if (currentDiffIndex.value > 0) {
    currentDiffIndex.value--
    scrollToDiff(currentDiffIndex.value)
  }
}

const nextDiff = () => {
  if (currentDiffIndex.value < diffPositions.value.length - 1) {
    currentDiffIndex.value++
    scrollToDiff(currentDiffIndex.value)
  }
}

const scrollToDiff = (index) => {
  nextTick(() => {
    const element = document.querySelector(`[data-index="${diffPositions.value[index].index}"]`)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  })
}

// 工具函数
const getLineCount = (text) => {
  return text.split('\n').length
}
</script>

<style scoped>
.text-diff {
  min-height: 100vh;
  background: #f8fafc;
}

/* 头部区域 */
.tool-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 30px 20px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}


.tool-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 16px 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.tool-description {
  opacity: 0.9;
  font-size: 1.1rem;
  margin: 0;
}

/* 工具栏 */
.toolbar {
  background: white;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  gap: 20px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 内容区域 */
.content-area {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.input-panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.input-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
}

.panel-actions {
  display: flex;
  gap: 8px;
}

.editor-container {
  position: relative;
  height: 400px;
  display: flex;
  flex-direction: column;
}

.text-editor {
  flex: 1;
  border: none;
  outline: none;
  padding: 20px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  background: #f9fafb;
  color: #374151;
}

.editor-info {
  padding: 12px 20px;
  background: #f3f4f6;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  color: #6b7280;
}

/* 比对结果 */
.diff-result {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.result-header {
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.result-header h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
}

.result-stats {
  display: flex;
  gap: 16px;
  align-items: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.stat-item.added {
  color: #059669;
}

.stat-item.removed {
  color: #dc2626;
}

.stat-item.modified {
  color: #d97706;
}

/* 比对视图 */
.side-by-side-view {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.diff-panel {
  border-right: 1px solid #e5e7eb;
  max-height: 600px;
  overflow: auto;
}

.diff-panel:last-child {
  border-right: none;
}

.unified-view {
  max-height: 600px;
  overflow: auto;
}

.diff-content {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

:deep(.diff-line) {
  padding: 4px 12px;
  margin: 0;
  border-left: 3px solid transparent;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  line-height: 1.8;
}

:deep(.diff-added) {
  background-color: #d1fae5;
  color: #065f46;
  border-left-color: #10b981;
  display: inline;
  padding: 2px 4px;
  margin: 0 1px;
  border-radius: 3px;
}

:deep(.diff-removed) {
  background-color: #fee2e2;
  color: #991b1b;
  border-left-color: #ef4444;
  display: inline;
  padding: 2px 4px;
  margin: 0 1px;
  border-radius: 3px;
  text-decoration: line-through;
}

:deep(.diff-equal) {
  background-color: transparent;
  color: #374151;
  display: inline;
}

:deep(.diff-prefix) {
  margin-right: 8px;
  font-weight: 700;
}

/* 差异导航 */
.diff-navigation {
  padding: 16px 20px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.nav-info {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #6b7280;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 1.1rem;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .input-panels {
    grid-template-columns: 1fr;
  }
  
  .side-by-side-view {
    grid-template-columns: 1fr;
  }
  
  .diff-panel {
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .diff-panel:last-child {
    border-bottom: none;
  }
}

@media (max-width: 768px) {
  .tool-header {
    padding: 20px 15px;
  }
  
  .tool-title {
    font-size: 1.5rem;
  }
  
  .toolbar {
    padding: 15px;
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .content-area {
    padding: 15px;
  }
  
  .editor-container {
    height: 300px;
  }
  
  .panel-header {
    padding: 15px;
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .panel-actions {
    justify-content: center;
  }
  
  .text-editor {
    padding: 15px;
    font-size: 13px;
  }
  
  .result-header {
    padding: 15px;
    flex-direction: column;
    align-items: stretch;
  }
  
  .result-stats {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .result-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .diff-navigation {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
