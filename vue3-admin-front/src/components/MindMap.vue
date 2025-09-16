<template>
  <div class="mindmap-container" ref="containerRef">
    <div 
      :id="containerId" 
      class="mindmap-canvas"
      :style="{ 
        width: width, 
        height: height,
        background: '#fff',
        border: '1px solid #e8e8e8',
        borderRadius: '4px'
      }"
    ></div>
    
    <!-- 工具栏 -->
    <div v-if="showToolbar" class="mindmap-toolbar">
      <el-button-group>
        <el-button size="small" @click="zoomIn" title="放大">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
        <el-button size="small" @click="zoomOut" title="缩小">
          <el-icon><ZoomOut /></el-icon>
        </el-button>
        <el-button size="small" @click="resetZoom" title="重置缩放">
          {{ Math.round(currentZoom * 100) }}%
        </el-button>
        <el-button size="small" @click="fitView" title="适应画布">
          <el-icon><FullScreen /></el-icon>
        </el-button>
      </el-button-group>
      
      <el-button-group v-if="editable" style="margin-left: 8px;">
        <el-button size="small" @click="addNode" title="添加节点">
          <el-icon><Plus /></el-icon>
        </el-button>
        <el-button size="small" @click="deleteNode" title="删除节点" :disabled="!selectedNode">
          <el-icon><Delete /></el-icon>
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 节点编辑对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      title="编辑节点" 
      width="400px"
      @close="cancelEdit"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="节点名称">
          <el-input 
            v-model="editForm.title" 
            placeholder="请输入节点名称"
            @keyup.enter="confirmEdit"
          />
        </el-form-item>
        <el-form-item label="节点描述">
          <el-input 
            v-model="editForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入节点描述（可选）"
          />
        </el-form-item>
        <el-form-item label="节点类型">
          <el-select v-model="editForm.nodeType" style="width: 100%">
            <el-option label="普通节点" value="normal" />
            <el-option label="重要节点" value="important" />
            <el-option label="分类节点" value="category" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { Graph } from '@antv/g6'
import { ElMessage } from 'element-plus'
import { ZoomIn, ZoomOut, FullScreen, Plus, Delete } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  data: {
    type: Object,
    default: () => ({ nodes: [], edges: [] })
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '600px'
  },
  editable: {
    type: Boolean,
    default: false
  },
  showToolbar: {
    type: Boolean,
    default: true
  },
  layout: {
    type: String,
    default: 'mindmap'
  }
})

// Emits
const emit = defineEmits(['node-click', 'node-dblclick', 'data-change', 'node-add', 'node-delete', 'node-update'])

// Refs
const containerRef = ref()
const containerId = ref(`mindmap-${Date.now()}`)

// Data
const graph = ref(null)
const currentZoom = ref(1)
const selectedNode = ref(null)
const showEditDialog = ref(false)
const editForm = reactive({
  id: '',
  title: '',
  description: '',
  nodeType: 'normal'
})

// Methods
const createGraph = () => {
  const container = document.getElementById(containerId.value)
  if (!container) return null
  
  const newGraph = new Graph({
    container: containerId.value,
    width: container.offsetWidth,
    height: container.offsetHeight,
    layout: {
      type: 'dagre',
      direction: 'LR',
      rankdir: 'LR',
      nodesep: 30,
      ranksep: 50
    },
    modes: {
      default: ['zoom-canvas', 'drag-canvas', 'drag-node']
    },
         defaultNode: {
       type: 'rect',
       size: [120, 50],
       style: {
         fill: 'l(0) 0:#ffffff 1:#f0f8ff',
         stroke: '#4A90E2',
         lineWidth: 2,
         radius: 12,
         shadowColor: 'rgba(74, 144, 226, 0.15)',
         shadowBlur: 8,
         shadowOffsetX: 2,
         shadowOffsetY: 2
       },
       labelCfg: {
         style: {
           fontSize: 14,
           fill: '#2c3e50',
           fontWeight: 500
         }
       }
     },
         defaultEdge: {
       type: 'cubic-horizontal',
       style: {
         stroke: '#95a5a6',
         lineWidth: 3,
         opacity: 0.7,
         endArrow: false,
         lineDash: [0],
         shadowColor: 'rgba(149, 165, 166, 0.3)',
         shadowBlur: 2,
         shadowOffsetX: 1,
         shadowOffsetY: 1
       }
     },
    nodeStateStyles: {
      selected: {
        stroke: '#1890FF',
        lineWidth: 2,
        fill: '#BAE7FF'
      },
      hover: {
        stroke: '#40A9FF'
      }
    }
  })

  return newGraph
}

const initGraph = () => {
  console.log('🚀 初始化图表')
  
  // 销毁之前的图实例
  if (graph.value) {
    try {
      graph.value.destroy()
    } catch (e) {
      console.warn('销毁图表时出错:', e)
    }
    graph.value = null
  }

  if (!containerRef.value) {
    console.warn('容器不存在')
    return
  }

  const formattedData = formatData(props.data)
  if (!formattedData.nodes || formattedData.nodes.length === 0) {
    console.log('无节点数据')
    return
  }

  try {
    graph.value = createGraph()
    
    if (!graph.value) {
      console.error('图表创建失败')
      return
    }

    // 绑定事件
    bindEvents()
    
    // 加载数据
    graph.value.data(formattedData)
    graph.value.render()
    
    // 自适应视图 - 多次尝试确保正确定位
    setTimeout(() => {
      if (graph.value) {
        graph.value.fitView(30)
        // 延迟再次调用确保正确定位
        setTimeout(() => {
          if (graph.value) {
            graph.value.fitView(30)
          }
        }, 200)
        setTimeout(() => {
          if (graph.value) {
            graph.value.fitView(30)
          }
        }, 500)
      }
    }, 100)
    
    console.log('✅ 图表初始化完成')
    
  } catch (error) {
    console.error('创建图表失败:', error)
    ElMessage.error('图表初始化失败')
  }
}

const bindEvents = () => {
  if (!graph.value) return

  graph.value.on('node:click', (event) => {
    try {
      const { item } = event
      const nodeData = item.getModel()
      selectedNode.value = item
      
      // 清除所有选中状态
      graph.value.clearItemStates()
      // 设置当前节点为选中状态
      graph.value.setItemState(item, 'selected', true)
      
      emit('node-click', nodeData)
    } catch (e) {
      console.error('处理节点点击事件失败:', e)
    }
  })

  graph.value.on('node:dblclick', (event) => {
    if (props.editable) {
      try {
        const { item } = event
        const nodeData = item.getModel()
        editNode(nodeData.id, nodeData)
      } catch (e) {
        console.error('处理节点双击事件失败:', e)
      }
    }
  })

  graph.value.on('canvas:click', () => {
    if (selectedNode.value) {
      graph.value.clearItemStates()
      selectedNode.value = null
    }
  })

  graph.value.on('viewportchange', () => {
    try {
      currentZoom.value = graph.value.getZoom() || 1
    } catch (e) {
      console.warn('获取缩放级别失败:', e)
    }
  })
}

const formatData = (rawData) => {
  if (!rawData) return { nodes: [], edges: [] }
  
  const nodes = (rawData.nodes || []).map(node => {
    const nodeId = String(node.id || node.nodeId)
    const title = node.title || node.name || node.label || `节点${node.id}`
    const nodeType = node.nodeType || node.type || 'normal'
    const colorInfo = getNodeColor(nodeType, nodeId)
    
    return {
      id: nodeId,
      label: title,
      type: 'rect',
      size: [Math.max(title.length * 10 + 40, 120), 50],
      style: {
        fill: colorInfo.fill,
        stroke: colorInfo.stroke,
        lineWidth: 2,
        radius: 12,
        opacity: colorInfo.opacity,
        shadowColor: `${colorInfo.stroke}30`,
        shadowBlur: 6,
        shadowOffsetX: 2,
        shadowOffsetY: 2
      },
      // 保存原始数据
      title: title,
      description: node.description || node.content || '',
      nodeType: nodeType
    }
  })

  const edges = (rawData.edges || []).map(edge => ({
    id: edge.id || `${edge.source}-${edge.target}`,
    source: String(edge.source),
    target: String(edge.target),
    type: 'line'
  }))

  return { nodes, edges }
}

// 定义美观的颜色组合
const colorPalettes = [
  { bg: 'l(0) 0:#fff5f5 1:#fed7e2', border: '#f56565' }, // 红色
  { bg: 'l(0) 0:#fffaf0 1:#fbd38d', border: '#ed8936' }, // 橙色  
  { bg: 'l(0) 0:#fffff0 1:#faf089', border: '#d69e2e' }, // 黄色
  { bg: 'l(0) 0:#f0fff4 1:#9ae6b4', border: '#38a169' }, // 绿色
  { bg: 'l(0) 0:#e6fffa 1:#81e6d9', border: '#319795' }, // 青色
  { bg: 'l(0) 0:#ebf8ff 1:#90cdf4', border: '#3182ce' }, // 蓝色
  { bg: 'l(0) 0:#f7fafc 1:#cbd5e0', border: '#4a5568' }, // 灰色
  { bg: 'l(0) 0:#faf5ff 1:#d6bcfa', border: '#805ad5' }, // 紫色
  { bg: 'l(0) 0:#fff5f7 1:#feb2d2', border: '#d53f8c' }, // 粉色
  { bg: 'l(0) 0:#f0f4f8 1:#bee3f8', border: '#2b6cb0' }  // 淡蓝色
]

const getNodeColor = (nodeType, nodeId) => {
  // 根据节点ID生成一个稳定的随机索引
  const hash = nodeId.split('').reduce((a, b) => {
    a = ((a << 5) - a) + b.charCodeAt(0)
    return a & a
  }, 0)
  
  const paletteIndex = Math.abs(hash) % colorPalettes.length
  const palette = colorPalettes[paletteIndex]
  
  // 根据节点类型调整透明度
  const typeMultiplier = {
    normal: 1,
    important: 0.9,
    category: 0.8
  }
  
  return {
    fill: palette.bg,
    stroke: palette.border,
    opacity: typeMultiplier[nodeType] || 1
  }
}

// 工具栏方法
const zoomIn = () => {
  if (!graph.value) return
  const zoom = graph.value.getZoom()
  graph.value.zoomTo(Math.min(zoom * 1.2, 3))
}

const zoomOut = () => {
  if (!graph.value) return
  const zoom = graph.value.getZoom()
  graph.value.zoomTo(Math.max(zoom * 0.8, 0.1))
}

const resetZoom = () => {
  if (!graph.value) return
  graph.value.zoomTo(1)
}

const fitView = () => {
  if (!graph.value) return
  graph.value.fitView(20)
}

// 编辑方法
const addNode = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个父节点')
    return
  }
  
  editForm.id = ''
  editForm.title = ''
  editForm.description = ''
  editForm.nodeType = 'normal'
  showEditDialog.value = true
}

const editNode = (nodeId, nodeData) => {
  editForm.id = nodeId
  editForm.title = nodeData.title || ''
  editForm.description = nodeData.description || ''
  editForm.nodeType = nodeData.nodeType || 'normal'
  showEditDialog.value = true
}

const deleteNode = () => {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择要删除的节点')
    return
  }
  
  try {
    const nodeData = selectedNode.value.getModel()
    emit('node-delete', nodeData)
    selectedNode.value = null
  } catch (e) {
    console.error('删除节点失败:', e)
  }
}

const confirmEdit = () => {
  if (!editForm.title.trim()) {
    ElMessage.warning('请输入节点名称')
    return
  }
  
  const nodeData = {
    id: editForm.id || `node_${Date.now()}`,
    title: editForm.title,
    description: editForm.description,
    nodeType: editForm.nodeType
  }
  
  if (editForm.id) {
    emit('node-update', nodeData)
  } else {
    if (selectedNode.value) {
      const parentData = selectedNode.value.getModel()
      nodeData.parentId = parentData.id
    }
    emit('node-add', nodeData)
  }
  
  showEditDialog.value = false
}

const cancelEdit = () => {
  showEditDialog.value = false
}

// Watch数据变化（只监听数据引用变化，不深度监听）
let updateTimer = null
const lastDataHash = ref('')
const watchEnabled = ref(true) // 添加开关控制

const getDataHash = (data) => {
  if (!data || !data.nodes) return ''
  return `${data.nodes.length}-${data.edges?.length || 0}`
}

watch(() => props.data, (newData) => {
  if (!watchEnabled.value) {
    console.log('📊 MindMap watch已禁用，跳过更新')
    return
  }
  
  const newHash = getDataHash(newData)
  
  // 只有数据实际变化时才更新
  if (newHash !== lastDataHash.value) {
    console.log('📊 MindMap数据真正变化，准备更新')
    console.log('📊 节点数:', newData?.nodes?.length || 0)
    
    lastDataHash.value = newHash
    
    if (updateTimer) clearTimeout(updateTimer)
    updateTimer = setTimeout(() => {
      console.log('📊 执行MindMap更新')
      initGraph()
    }, 300)
  } else {
    console.log('📊 MindMap数据无变化，跳过更新')
  }
}, { immediate: false }) // 移除deep监听，提高性能

// Lifecycle
onMounted(() => {
  nextTick(() => {
    initGraph()
  })
})

onUnmounted(() => {
  if (updateTimer) clearTimeout(updateTimer)
  if (graph.value) {
    try {
      graph.value.destroy()
    } catch (e) {
      console.warn('卸载时销毁图表失败:', e)
    }
  }
})

// 暴露方法
defineExpose({
  getGraph: () => graph.value,
  fitView,
  zoomIn,
  zoomOut,
  resetZoom,
  refresh: initGraph,
  enableWatch: () => { watchEnabled.value = true },
  disableWatch: () => { watchEnabled.value = false }
})
</script>

<style scoped>
.mindmap-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.mindmap-canvas {
  position: relative;
}

.mindmap-toolbar {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-button-group {
  vertical-align: top;
}

/* 覆盖Element Plus样式 */
:deep(.el-button--small) {
  padding: 4px 8px;
  font-size: 12px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style> 