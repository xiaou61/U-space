<template>
  <div class="knowledge-editor-container">
    <!-- 顶部工具栏 -->
    <div class="editor-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <div class="breadcrumb">
          <span class="map-title">{{ mapInfo.title || '知识图谱' }}</span>
          <el-tag v-if="mapInfo.status !== undefined" :type="getStatusTagType(mapInfo.status)" size="small">
            {{ getStatusText(mapInfo.status) }}
          </el-tag>
        </div>
      </div>
      
      <div class="header-right">
        <el-button-group>
          <el-button :icon="ZoomOut" @click="handleZoomOut" title="缩小">缩小</el-button>
          <el-button @click="handleResetZoom" title="适应画布">{{ Math.round(zoomLevel * 100) }}%</el-button>
          <el-button :icon="ZoomIn" @click="handleZoomIn" title="放大">放大</el-button>
        </el-button-group>
        <el-button :icon="Search" @click="showSearchDialog = true">搜索节点</el-button>
        <el-button type="primary" :icon="Check" @click="handleSave">保存</el-button>
      </div>
    </div>

    <div class="editor-content">
      <!-- 左侧节点树面板 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <h3 v-if="!sidebarCollapsed">节点树</h3>
          <el-button 
            text 
            :icon="sidebarCollapsed ? Expand : Fold" 
            @click="sidebarCollapsed = !sidebarCollapsed"
          />
        </div>
        
        <div v-if="!sidebarCollapsed" class="sidebar-content">
          <!-- 节点树 -->
          <div class="node-tree">
            <div class="tree-toolbar">
              <el-button size="small" :icon="Plus" @click="handleAddRootNode">
                添加根节点
              </el-button>
            </div>
            
            <el-tree
              ref="treeRef"
              :data="nodeTree"
              node-key="id"
              :props="treeProps"
              :expand-on-click-node="false"
              :highlight-current="true"
              @node-click="handleNodeClick"
              @node-contextmenu="handleNodeRightClick"
            >
              <template #default="{ node, data }">
                <div class="tree-node" :class="{ active: selectedNodeId === data.id }">
                  <el-icon class="node-icon" :class="`node-type-${data.nodeType}`">
                    <component :is="getNodeIcon(data.nodeType)" />
                  </el-icon>
                  <span class="node-label">{{ data.title }}</span>
                  <div class="node-actions">
                    <el-button
                      size="small"
                      text
                      :icon="Plus"
                      @click.stop="handleAddChildNode(data)"
                      title="添加子节点"
                    />
                    <el-button
                      size="small"
                      text
                      :icon="Edit"
                      @click.stop="handleEditNode(data)"
                      title="编辑节点"
                    />
                    <el-button
                      size="small"
                      text
                      :icon="Delete"
                      @click.stop="handleDeleteNode(data)"
                      title="删除节点"
                    />
                  </div>
                </div>
              </template>
            </el-tree>
          </div>
          
          <!-- 节点详情编辑 -->
          <div v-if="selectedNode" class="node-editor">
            <div class="editor-header">
              <h4>节点编辑</h4>
            </div>
            <el-form
              ref="nodeFormRef"
              :model="nodeForm"
              :rules="nodeFormRules"
              label-width="80px"
              size="small"
            >
              <el-form-item label="标题" prop="title">
                <el-input v-model="nodeForm.title" placeholder="请输入节点标题" />
              </el-form-item>
              
              <el-form-item label="类型" prop="nodeType">
                <el-select v-model="nodeForm.nodeType" style="width: 100%;">
                  <el-option label="普通" :value="1" />
                  <el-option label="重点" :value="2" />
                  <el-option label="难点" :value="3" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="展开" prop="isExpanded">
                <el-switch v-model="nodeForm.isExpanded" />
              </el-form-item>
              
              <el-form-item label="链接" prop="url">
                <el-input
                  v-model="nodeForm.url"
                  placeholder="请输入飞书云文档链接"
                  clearable
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" size="small" @click="handleSaveNode">
                  保存节点
                </el-button>
                <el-button size="small" @click="handleCancelEdit">
                  取消
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 主编辑画布 -->
      <div class="canvas-container">
        <div class="canvas-toolbar">
          <div class="canvas-info">
            <span>节点总数: {{ nodeCount }}</span>
            <span style="margin-left: 16px;">已选择: {{ selectedNodeId ? '1' : '0' }} 个节点</span>
          </div>
        </div>
        
        <!-- G6思维导图组件 -->
        <MindMap
          ref="mindMapRef"
          :data="mindMapData"
          :editable="true"
          :show-toolbar="false"
          height="100%"
          @node-click="handleMindMapNodeClick"
          @node-dblclick="handleEditNode"
          @node-add="handleAddNodeFromMindMap"
          @node-delete="handleDeleteNodeFromMindMap"
          @node-update="handleUpdateNodeFromMindMap"
        />
        
        <div v-if="!nodeTree.length" class="empty-canvas">
          <el-icon size="64" color="#c0c4cc"><Document /></el-icon>
          <p>暂无节点，点击"添加根节点"开始创建</p>
        </div>
      </div>
    </div>

    <!-- 搜索节点对话框 -->
    <el-dialog
      v-model="showSearchDialog"
      title="搜索节点"
      width="500px"
    >
      <el-input
        v-model="searchKeyword"
        placeholder="输入关键词搜索节点"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      
      <div v-if="searchResults.length" class="search-results" style="margin-top: 16px;">
        <div 
          v-for="result in searchResults" 
          :key="result.id"
          class="search-result-item"
          @click="handleSelectSearchResult(result)"
        >
          <div class="result-title">{{ result.title }}</div>
          <div class="result-content">{{ result.url || '暂无链接' }}</div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showSearchDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </template>
    </el-dialog>

    <!-- 右键菜单 -->
    <el-menu
      v-show="contextMenuVisible"
      class="context-menu"
      :style="contextMenuStyle"
      mode="vertical"
    >
      <el-menu-item index="add-child" @click="handleContextMenuAdd">
        <el-icon><Plus /></el-icon>
        添加子节点
      </el-menu-item>
      <el-menu-item index="edit" @click="handleContextMenuEdit">
        <el-icon><Edit /></el-icon>
        编辑节点
      </el-menu-item>
      <el-menu-item index="delete" @click="handleContextMenuDelete">
        <el-icon><Delete /></el-icon>
        删除节点
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Check, ZoomIn, ZoomOut, Search, Plus, Edit, Delete,
  Expand, Fold, Document, Setting, Star, Warning
} from '@element-plus/icons-vue'
import {
  getKnowledgeMapById,
  getKnowledgeNodeTree,
  createKnowledgeNode,
  updateKnowledgeNode,
  deleteKnowledgeNode,
  searchKnowledgeNodes
} from '@/api/knowledge'
import MindMap from '@/components/MindMap.vue'

const route = useRoute()
const router = useRouter()

// 基础数据
const mapId = ref(route.params.id)
const mapInfo = ref({})
const nodeTree = ref([])
const selectedNode = ref(null)
const selectedNodeId = ref(null)

// UI状态
const loading = ref(false)
const sidebarCollapsed = ref(false)
const zoomLevel = ref(1)
const showSearchDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])

// 上下文菜单
const contextMenuVisible = ref(false)
const contextMenuStyle = ref({})
const contextMenuNode = ref(null)

// 表单相关
const nodeForm = reactive({
  title: '',
  url: '',
  nodeType: 1,
  isExpanded: true
})

const nodeFormRef = ref()

const nodeFormRules = {
  title: [
    { required: true, message: '请输入节点标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ]
}

// 树配置
const treeProps = {
  children: 'children',
  label: 'title'
}

const treeRef = ref()
const mindMapRef = ref()

// 计算属性
const nodeCount = computed(() => {
  const countNodes = (nodes) => {
    let count = 0
    for (const node of nodes) {
      count++
      if (node.children && node.children.length) {
        count += countNodes(node.children)
      }
    }
    return count
  }
  return countNodes(nodeTree.value)
})

// 转换为G6格式的思维导图数据 - 添加更强的缓存机制
let cachedMindMapData = null
let lastNodeTreeHash = ''

const mindMapData = computed(() => {
  if (!nodeTree.value || nodeTree.value.length === 0) {
    return { nodes: [], edges: [] }
  }
  
  // 简单的哈希检查，避免不必要的重计算
  const currentHash = JSON.stringify(nodeTree.value.map(n => ({ id: n.id, title: n.title, children: n.children?.length || 0 })))
  
  if (currentHash === lastNodeTreeHash && cachedMindMapData) {
    console.log('📊 使用缓存的mindMapData')
    return cachedMindMapData
  }
  
  console.log('📊 重新计算mindMapData')
  lastNodeTreeHash = currentHash
  
  const nodes = []
  const edges = []
  
  const processNode = (node, parent = null) => {
    nodes.push({
      id: node.id.toString(),
      title: node.title,
      description: node.url || '',
      nodeType: getNodeTypeString(node.nodeType)
    })
    
    if (parent) {
      edges.push({
        id: `edge-${parent.id}-${node.id}`,
        source: parent.id.toString(),
        target: node.id.toString()
      })
    }
    
    if (node.children && node.children.length) {
      node.children.forEach(child => processNode(child, node))
    }
  }
  
  try {
    nodeTree.value.forEach(rootNode => processNode(rootNode))
    cachedMindMapData = { nodes, edges }
    console.log('📊 mindMapData计算完成', { nodes: nodes.length, edges: edges.length })
    return cachedMindMapData
  } catch (error) {
    console.error('📊 mindMapData计算出错:', error)
    return { nodes: [], edges: [] }
  }
})

const getNodeTypeString = (nodeType) => {
  const typeMap = { 1: 'normal', 2: 'important', 3: 'category' }
  return typeMap[nodeType] || 'normal'
}

// 方法
const fetchMapInfo = async () => {
  try {
    const data = await getKnowledgeMapById(mapId.value)
    mapInfo.value = data
  } catch (error) {
    ElMessage.error('获取图谱信息失败')
  }
}

const fetchNodeTree = async () => {
  try {
    loading.value = true
    const data = await getKnowledgeNodeTree(mapId.value)
    nodeTree.value = data
    
    // 清理缓存，确保数据更新
    cachedMindMapData = null
    lastNodeTreeHash = ''
    
    console.log('🔄 nodeTree更新完成', data.length)
    
    // 更新节点Map缓存
    refreshNodeMap()
    
  } catch (error) {
    ElMessage.error('获取节点树失败')
  } finally {
    loading.value = false
  }
}

const initMindMap = () => {
  // G6思维导图通过MindMap组件自动初始化
  // 数据通过mindMapData计算属性自动同步
}

// 优化节点查找性能 - 使用Map缓存
const nodeMap = new Map()
const updateNodeMap = () => {
  console.log('🗺️ 更新nodeMap缓存')
  nodeMap.clear()
  const buildMap = (nodes) => {
    for (const node of nodes) {
      // 存储数据副本，避免直接引用
      nodeMap.set(node.id.toString(), { ...node })
      if (node.children && node.children.length) {
        buildMap(node.children)
      }
    }
  }
  buildMap(nodeTree.value)
}

// 手动更新缓存，移除深度监听避免性能问题
const refreshNodeMap = () => {
  console.log('🔄 手动刷新nodeMap')
  updateNodeMap()
}

const handleNodeClick = (data) => {
  console.log('🖱️ 左侧树节点点击:', data.id)
  
  selectedNodeId.value = data.id
  selectedNode.value = { ...data } // 使用副本避免引用问题
  
  // 使用Map快速查找，避免递归遍历
  const fullNodeData = nodeMap.get(data.id.toString())
  
  if (fullNodeData) {
    // 填充编辑表单 - 使用数据副本
    Object.assign(nodeForm, {
      title: fullNodeData.title,
      url: fullNodeData.url,
      nodeType: fullNodeData.nodeType,
      isExpanded: fullNodeData.isExpanded
    })
  }
}

// 专门处理MindMap组件中节点点击的方法
const handleMindMapNodeClick = (nodeData) => {
  console.log('🎯 MindMap节点点击:', nodeData.id || nodeData.title)
  
  // 简化处理，只更新选中状态，避免复杂操作
  if (nodeData.id) {
    selectedNodeId.value = nodeData.id.toString()
    
    // 使用Map快速查找完整数据
    const fullNodeData = nodeMap.get(nodeData.id.toString())
    if (fullNodeData) {
      selectedNode.value = { ...fullNodeData } // 使用副本
      
      // 填充编辑表单
      Object.assign(nodeForm, {
        title: fullNodeData.title,
        url: fullNodeData.url,
        nodeType: fullNodeData.nodeType,
        isExpanded: fullNodeData.isExpanded
      })
      
      // 同步左侧树的选中状态
      nextTick(() => {
        treeRef.value?.setCurrentKey(fullNodeData.id)
      })
    }
  }
}

// 处理思维导图组件的节点添加事件
const handleAddNodeFromMindMap = async (nodeData) => {
  try {
    const createData = {
      parentId: nodeData.parentId || 0,
      title: nodeData.title,
      url: nodeData.description || '',
      nodeType: getNodeTypeNumber(nodeData.nodeType),
      sortOrder: 0,
      isExpanded: true
    }
    
    await createKnowledgeNode(mapId.value, createData)
    ElMessage.success('节点创建成功')
    
    // 刷新节点树
    await fetchNodeTree()
    
  } catch (error) {
    ElMessage.error('创建节点失败')
  }
}

// 处理思维导图组件的节点删除事件
const handleDeleteNodeFromMindMap = async (nodeData) => {
  try {
    await ElMessageBox.confirm(`确认删除节点"${nodeData.title}"？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteKnowledgeNode(parseInt(nodeData.id))
    ElMessage.success('节点删除成功')
    
    // 清空选中状态
    if (selectedNodeId.value === nodeData.id) {
      selectedNodeId.value = null
      selectedNode.value = null
      resetNodeForm()
    }
    
    // 刷新节点树
    await fetchNodeTree()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除节点失败')
    }
  }
}

// 处理思维导图组件的节点更新事件
const handleUpdateNodeFromMindMap = async (nodeData) => {
  try {
    const updateData = {
      title: nodeData.title,
      url: nodeData.description || '',
      nodeType: getNodeTypeNumber(nodeData.nodeType),
      isExpanded: true
    }
    
    await updateKnowledgeNode(parseInt(nodeData.id), updateData)
    ElMessage.success('节点更新成功')
    
    // 刷新节点树
    await fetchNodeTree()
    
  } catch (error) {
    ElMessage.error('更新节点失败')
  }
}

const getNodeTypeNumber = (nodeTypeString) => {
  const typeMap = { 'normal': 1, 'important': 2, 'category': 3 }
  return typeMap[nodeTypeString] || 1
}

const handleAddRootNode = () => {
  handleAddChildNode({ id: 0, title: '根节点' })
}

const handleAddChildNode = async (parentNode) => {
  try {
    const { value: title } = await ElMessageBox.prompt('请输入节点标题', '添加节点', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: '节点标题',
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '请输入节点标题'
        }
        if (value.length > 200) {
          return '标题长度不能超过200个字符'
        }
        return true
      }
    })
    
    const nodeData = {
      parentId: parentNode.id,
      title: title.trim(),
      url: '',
      nodeType: 1,
      sortOrder: 0,
      isExpanded: true
    }
    
    await createKnowledgeNode(mapId.value, nodeData)
    ElMessage.success('节点创建成功')
    
    // 刷新节点树
    await fetchNodeTree()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('创建节点失败')
    }
  }
}

const handleEditNode = (data) => {
  handleNodeClick(data)
}

const handleDeleteNode = async (data) => {
  try {
    await ElMessageBox.confirm(`确认删除节点"${data.title}"？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteKnowledgeNode(data.id)
    ElMessage.success('节点删除成功')
    
    // 清空选中状态
    if (selectedNodeId.value === data.id) {
      selectedNodeId.value = null
      selectedNode.value = null
      resetNodeForm()
    }
    
    // 刷新节点树
    await fetchNodeTree()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除节点失败')
    }
  }
}

const handleSaveNode = async () => {
  console.log('💾 handleSaveNode开始执行')
  
  if (!selectedNode.value) {
    console.log('❌ selectedNode.value为空，退出')
    return
  }
  
  console.log('💾 开始保存节点:', selectedNode.value.id)
  console.log('💾 表单数据:', nodeForm)
  
  try {
    // 临时禁用MindMap的watch监听
    console.log('🔒 禁用MindMap监听')
    mindMapRef.value?.disableWatch()
    
    // 临时跳过表单验证，直接保存
    console.log('💾 调用API保存节点...')
    await updateKnowledgeNode(selectedNode.value.id, nodeForm)
    
    console.log('✅ API调用成功')
    ElMessage.success('节点保存成功')
    console.log('✅ 节点保存完成，方法即将退出')
    
    // 重新启用MindMap监听
    setTimeout(() => {
      console.log('🔓 重新启用MindMap监听')
      mindMapRef.value?.enableWatch()
    }, 1000)
    
  } catch (error) {
    console.error('❌ 保存节点失败:', error)
    ElMessage.error('保存节点失败: ' + (error.message || '未知错误'))
    
    // 确保在错误情况下也重新启用监听
    mindMapRef.value?.enableWatch()
  }
  
  console.log('💾 handleSaveNode方法执行完毕')
}

const handleCancelEdit = () => {
  selectedNodeId.value = null
  selectedNode.value = null
  resetNodeForm()
}

const handleSave = () => {
  ElMessage.success('图谱保存成功')
}

// 防抖的刷新函数，避免频繁调用
let refreshTimer = null
const debouncedRefresh = () => {
  if (refreshTimer) clearTimeout(refreshTimer)
  refreshTimer = setTimeout(() => {
    console.log('🔄 执行延迟刷新')
    fetchNodeTree()
  }, 800) // 800ms防抖
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  try {
    const data = await searchKnowledgeNodes(mapId.value, searchKeyword.value)
    searchResults.value = data
    
    if (!data.length) {
      ElMessage.info('未找到相关节点')
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const handleSelectSearchResult = (result) => {
  showSearchDialog.value = false
  searchKeyword.value = ''
  searchResults.value = []
  
  // 选中节点并定位
  handleNodeClick(result)
  
  // 展开树到该节点
  treeRef.value?.setCurrentKey(result.id)
}

const handleZoomIn = () => {
  if (mindMapRef.value) {
    mindMapRef.value.zoomIn()
  }
}

const handleZoomOut = () => {
  if (mindMapRef.value) {
    mindMapRef.value.zoomOut()
  }
}

const handleResetZoom = () => {
  if (mindMapRef.value) {
    mindMapRef.value.resetZoom()
  }
}

const goBack = () => {
  console.log('🔙 准备返回列表，开始清理资源')
  
  try {
    // 主动清理定时器
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
      console.log('🧹 提前清理refreshTimer')
    }
    
    // 禁用MindMap监听
    if (mindMapRef.value) {
      mindMapRef.value.disableWatch?.()
      console.log('🧹 提前禁用MindMap监听')
    }
    
    // 清理缓存
    nodeMap.clear()
    cachedMindMapData = null
    lastNodeTreeHash = ''
    console.log('🧹 提前清理缓存')
    
    // 延迟跳转，确保清理完成
    setTimeout(() => {
      console.log('🔙 执行路由跳转')
      router.push('/knowledge/maps')
    }, 100)
    
  } catch (error) {
    console.error('清理资源时出错:', error)
    // 即使出错也要跳转
    router.push('/knowledge/maps')
  }
}

// 右键菜单相关
const handleNodeRightClick = (event, data) => {
  event.preventDefault()
  contextMenuNode.value = data
  showContextMenu(event)
}

const handleCanvasRightClick = (event) => {
  // 画布右键菜单逻辑
  console.log('画布右键', event)
}

const showContextMenu = (event) => {
  contextMenuVisible.value = true
  contextMenuStyle.value = {
    left: `${event.clientX}px`,
    top: `${event.clientY}px`,
    position: 'fixed',
    zIndex: 9999
  }
  
  // 点击其他地方隐藏菜单
  const hideMenu = () => {
    contextMenuVisible.value = false
    document.removeEventListener('click', hideMenu)
  }
  
  setTimeout(() => {
    document.addEventListener('click', hideMenu)
  }, 100)
}

const handleContextMenuAdd = () => {
  if (contextMenuNode.value) {
    handleAddChildNode(contextMenuNode.value)
  }
  contextMenuVisible.value = false
}

const handleContextMenuEdit = () => {
  if (contextMenuNode.value) {
    handleEditNode(contextMenuNode.value)
  }
  contextMenuVisible.value = false
}

const handleContextMenuDelete = () => {
  if (contextMenuNode.value) {
    handleDeleteNode(contextMenuNode.value)
  }
  contextMenuVisible.value = false
}

// 辅助方法
const getNodeIcon = (nodeType) => {
  const iconMap = {
    1: Document,  // 普通
    2: Star,      // 重点
    3: Warning    // 难点
  }
  return iconMap[nodeType] || Document
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'info',     // 草稿
    1: 'success',  // 已发布
    2: 'warning'   // 已隐藏
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '草稿',
    1: '已发布',
    2: '已隐藏'
  }
  return textMap[status] || '未知'
}

// 节点高亮由MindMap组件自动处理

const resetNodeForm = () => {
  Object.assign(nodeForm, {
    title: '',
    url: '',
    nodeType: 1,
    isExpanded: true
  })
  nodeFormRef.value?.resetFields()
}

// 生命周期
onMounted(async () => {
  await fetchMapInfo()
  await fetchNodeTree()
  // 初始化节点Map缓存
  refreshNodeMap()
})

// 组件销毁时清理资源
onUnmounted(() => {
  console.log('🧹 组件卸载，清理所有资源')
  
  // 清理定时器
  if (refreshTimer) {
    clearTimeout(refreshTimer)
    refreshTimer = null
    console.log('🧹 清理refreshTimer')
  }
  
  // 清理MindMap相关资源
  if (mindMapRef.value) {
    try {
      mindMapRef.value.disableWatch?.()
      console.log('🧹 禁用MindMap监听')
    } catch (e) {
      console.warn('清理MindMap时出错:', e)
    }
  }
  
  // 清理缓存
  nodeMap.clear()
  cachedMindMapData = null
  lastNodeTreeHash = ''
  
  // 重置状态
  selectedNodeId.value = null
  selectedNode.value = null
  nodeTree.value = []
  
  console.log('✅ 资源清理完成')
})

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId && newId !== mapId.value) {
    mapId.value = newId
    fetchMapInfo()
    fetchNodeTree()
  }
})
</script>

<style scoped>
.knowledge-editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 320px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 48px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.node-tree {
  border-bottom: 1px solid #e4e7ed;
  max-height: 50%;
  overflow-y: auto;
}

.tree-toolbar {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.tree-node:hover {
  background-color: #f5f7fa;
}

.tree-node.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.node-icon {
  font-size: 16px;
}

.node-icon.node-type-1 { color: #909399; }
.node-icon.node-type-2 { color: #f56c6c; }
.node-icon.node-type-3 { color: #e6a23c; }

.node-label {
  flex: 1;
  font-size: 13px;
  line-height: 1.4;
}

.node-actions {
  display: none;
  gap: 2px;
}

.tree-node:hover .node-actions {
  display: flex;
}

.node-editor {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.node-editor .editor-header {
  margin-bottom: 16px;
  padding: 0;
  border: none;
  box-shadow: none;
}

.node-editor h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  position: relative;
}

.canvas-toolbar {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.canvas-info {
  font-size: 12px;
  color: #909399;
}

.mind-map-canvas {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.empty-canvas {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

.empty-canvas p {
  margin: 16px 0 0 0;
  font-size: 14px;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.search-result-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.result-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.result-content {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 4px 0;
  min-width: 120px;
}

.context-menu .el-menu-item {
  height: 32px;
  line-height: 32px;
  padding: 0 16px;
  font-size: 13px;
}

/* Element Plus 样式覆盖 */
:deep(.el-tree-node__content) {
  height: auto;
  padding: 4px 8px;
}

:deep(.el-tree-node__expand-icon) {
  padding: 2px;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-size: 12px;
}
</style> 