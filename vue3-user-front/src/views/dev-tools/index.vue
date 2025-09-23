<template>
  <div class="dev-tools">
    <!-- 头部标题区域 -->
    <div class="tools-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="main-title">
            <el-icon class="title-icon"><Tools /></el-icon>
            程序员工具箱
          </h1>
          <p class="subtitle">提升开发效率的实用工具集合</p>
        </div>
      </div>
    </div>

    <!-- 工具卡片区域 -->
    <div class="tools-grid">
      <div class="tool-card json-tool" @click="navigateToTool('json')">
        <div class="card-header">
          <div class="tool-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="tool-badge">热门</div>
        </div>
        <div class="card-content">
          <h3 class="tool-title">JSON 工具</h3>
          <p class="tool-description">
            JSON格式化、验证、压缩和转换工具，支持语法高亮和错误定位
          </p>
          <div class="tool-features">
            <span class="feature-tag">格式化</span>
            <span class="feature-tag">验证</span>
            <span class="feature-tag">压缩</span>
            <span class="feature-tag">转换</span>
          </div>
        </div>
        <div class="card-footer">
          <el-button type="primary" size="large" class="tool-button">
            <el-icon><Right /></el-icon>
            立即使用
          </el-button>
        </div>
      </div>

      <div class="tool-card diff-tool" @click="navigateToTool('text-diff')">
        <div class="card-header">
          <div class="tool-icon">
            <el-icon><Operation /></el-icon>
          </div>
          <div class="tool-badge">实用</div>
        </div>
        <div class="card-content">
          <h3 class="tool-title">文本比对</h3>
          <p class="tool-description">
            智能文本差异对比工具，支持字符级、单词级、行级对比模式
          </p>
          <div class="tool-features">
            <span class="feature-tag">实时对比</span>
            <span class="feature-tag">差异高亮</span>
            <span class="feature-tag">多模式</span>
            <span class="feature-tag">导出</span>
          </div>
        </div>
        <div class="card-footer">
          <el-button type="primary" size="large" class="tool-button">
            <el-icon><Right /></el-icon>
            立即使用
          </el-button>
        </div>
      </div>

      <div class="tool-card translate-tool" @click="navigateToTool('translation')">
        <div class="card-header">
          <div class="tool-icon">
            <el-icon><ChatLineSquare /></el-icon>
          </div>
          <div class="tool-badge">强大</div>
        </div>
        <div class="card-content">
          <h3 class="tool-title">聚合翻译</h3>
          <p class="tool-description">
            集成多个翻译平台，同时获取多种翻译结果，选择最佳翻译
          </p>
          <div class="tool-features">
            <span class="feature-tag">多平台</span>
            <span class="feature-tag">对比</span>
            <span class="feature-tag">批量</span>
            <span class="feature-tag">专业</span>
          </div>
        </div>
        <div class="card-footer">
          <el-button type="primary" size="large" class="tool-button">
            <el-icon><Right /></el-icon>
            立即使用
          </el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { 
  Tools, Document, Operation, ChatLineSquare, Right
} from '@element-plus/icons-vue'

const router = useRouter()

// 导航到具体工具
const navigateToTool = (toolName) => {
  router.push(`/dev-tools/${toolName}`)
}
</script>

<style scoped>
.dev-tools {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow-x: hidden;
}

.dev-tools::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.dev-tools > * {
  position: relative;
  z-index: 1;
}

/* 头部区域 */
.tools-header {
  padding: 80px 20px 60px;
  text-align: center;
  color: white;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}


.main-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: linear-gradient(45deg, #fff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 3rem;
  color: #fbbf24;
}

.subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}


/* 工具卡片区域 */
.tools-grid {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 80px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
}

.tool-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.tool-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.tool-card:hover::before {
  transform: scaleX(1);
}

.tool-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.15);
}

.json-tool:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(147, 51, 234, 0.05));
}

.diff-tool:hover {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.05), rgba(5, 150, 105, 0.05));
}

.translate-tool:hover {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.05), rgba(217, 119, 6, 0.05));
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tool-icon {
  width: 60px;
  height: 60px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
}

.json-tool .tool-icon {
  background: linear-gradient(135deg, #3b82f6, #9333ea);
}

.diff-tool .tool-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.translate-tool .tool-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.tool-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
}

.json-tool .tool-badge {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.diff-tool .tool-badge {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.translate-tool .tool-badge {
  background: linear-gradient(135deg, #06b6d4, #0891b2);
}

.card-content {
  margin-bottom: 30px;
}

.tool-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.tool-description {
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 20px 0;
  font-size: 0.95rem;
}

.tool-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-tag {
  padding: 4px 12px;
  background: rgba(107, 114, 128, 0.1);
  border-radius: 12px;
  font-size: 0.8rem;
  color: #374151;
  font-weight: 500;
}

.card-footer {
  text-align: center;
}

.tool-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
}


/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2.5rem;
    flex-direction: column;
    gap: 8px;
  }

  .title-icon {
    font-size: 2rem;
  }



  .tools-grid {
    grid-template-columns: 1fr;
    padding: 0 15px 60px;
  }

  .tool-card {
    padding: 24px;
  }

}

@media (max-width: 480px) {
  .tools-header {
    padding: 60px 15px 40px;
  }

  .main-title {
    font-size: 2rem;
  }


  .tool-card {
    padding: 20px;
  }
}
</style>
