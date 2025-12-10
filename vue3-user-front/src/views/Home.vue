<template>
  <div class="home-container">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="floating-shapes">
          <div class="shape shape-1"></div>
          <div class="shape shape-2"></div>
          <div class="shape shape-3"></div>
          <div class="shape shape-4"></div>
          <div class="shape shape-5"></div>
        </div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">🚀 开发者成长社区</div>
        <h1 class="hero-title">
          <span class="gradient-text">Code Nest</span>
          <br />
          <span class="sub-title">程序员的技术成长乐园</span>
        </h1>
        <p class="hero-desc">
          集面试刷题、知识图谱、在线简历、代码工坊、技术博客、社区互动于一体的一站式开发者平台
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="action-btn primary-btn" @click="$router.push('/interview')">
            <el-icon><Reading /></el-icon>
            开始刷题
          </el-button>
          <el-button size="large" class="action-btn secondary-btn" @click="$router.push('/community')">
            <el-icon><ChatDotRound /></el-icon>
            进入社区
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">1000+</span>
            <span class="stat-label">面试题目</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">50+</span>
            <span class="stat-label">知识图谱</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">10+</span>
            <span class="stat-label">实用工具</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心功能区域 -->
    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-subtitle">全方位助力你的技术成长之路</p>
      </div>
      <div class="features-grid">
        <div 
          v-for="(feature, index) in features" 
          :key="feature.title" 
          class="feature-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="$router.push(feature.path)"
        >
          <div class="feature-icon" :style="{ background: feature.gradient }">
            <el-icon :size="28"><component :is="feature.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.desc }}</p>
          <div class="feature-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 快速入口区域 -->
    <section class="quick-access-section">
      <div class="section-header">
        <h2 class="section-title">快速入口</h2>
        <p class="section-subtitle">一键直达常用功能</p>
      </div>
      <div class="quick-access-grid">
        <div 
          v-for="item in quickAccess" 
          :key="item.title" 
          class="quick-item"
          @click="$router.push(item.path)"
        >
          <div class="quick-icon" :style="{ background: item.color }">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <span class="quick-title">{{ item.title }}</span>
        </div>
      </div>
    </section>

    <!-- 特色亮点区域 -->
    <section class="highlights-section">
      <div class="highlights-content">
        <div class="highlight-item">
          <div class="highlight-icon">
            <el-icon :size="32"><Cpu /></el-icon>
          </div>
          <div class="highlight-info">
            <h3>智能学习系统</h3>
            <p>基于知识图谱的学习路径规划，助你高效提升技术能力</p>
          </div>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">
            <el-icon :size="32"><Trophy /></el-icon>
          </div>
          <div class="highlight-info">
            <h3>积分激励体系</h3>
            <p>完成任务获取积分，参与抽奖赢取丰厚奖品</p>
          </div>
        </div>
        <div class="highlight-item">
          <div class="highlight-icon">
            <el-icon :size="32"><Connection /></el-icon>
          </div>
          <div class="highlight-info">
            <h3>实时互动社区</h3>
            <p>技术讨论、在线聊天、动态分享，与志同道合的开发者交流</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部 CTA -->
    <section class="cta-section">
      <div class="cta-content">
        <h2>准备好开始你的技术成长之旅了吗？</h2>
        <p>加入 Code Nest，与万千开发者一起学习进步</p>
        <el-button type="primary" size="large" class="cta-btn" @click="$router.push('/interview')">
          立即开始
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  Reading, ChatDotRound, ArrowRight, Document, Edit, 
  Monitor, Share, Bell, Trophy, Cpu, Connection,
  Compass, Coffee, TrendCharts, Ticket, ChatLineSquare, DataAnalysis, Calendar
} from '@element-plus/icons-vue'

// 核心功能数据
const features = ref([
  {
    title: '面试题库',
    desc: '海量面试真题，覆盖前端、后端、算法等热门方向，助你斩获心仪 Offer',
    icon: 'Reading',
    path: '/interview',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '知识图谱',
    desc: '可视化知识体系，理清技术脉络，构建完整的技术知识网络',
    icon: 'Share',
    path: '/knowledge',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '在线简历',
    desc: '专业简历模板，在线编辑导出，让你的简历脱颖而出',
    icon: 'Document',
    path: '/resume',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    title: '代码工坊',
    desc: '在线代码编辑器，支持多种语言，实时预览与分享你的创意',
    icon: 'Monitor',
    path: '/codepen',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  },
  {
    title: '技术博客',
    desc: 'Markdown 写作，记录学习心得，分享技术经验',
    icon: 'Edit',
    path: '/blog',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    title: '技术社区',
    desc: '问答交流、技术讨论，与全国开发者一起探讨技术难题',
    icon: 'ChatDotRound',
    path: '/community',
    gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)'
  }
])

// 快速入口数据
const quickAccess = ref([
  { title: '朋友圈', icon: 'ChatLineSquare', path: '/moments', color: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { title: '聊天室', icon: 'ChatDotRound', path: '/chat', color: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { title: '通知中心', icon: 'Bell', path: '/notification', color: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { title: '计划打卡', icon: 'Calendar', path: '/plan', color: 'linear-gradient(135deg, #f5af19, #f12711)' },
  { title: '我的积分', icon: 'Trophy', path: '/points', color: 'linear-gradient(135deg, #43e97b, #38f9d7)' },
  { title: '幸运抽奖', icon: 'Ticket', path: '/lottery', color: 'linear-gradient(135deg, #fa709a, #fee140)' },
  { title: '摸鱼工具', icon: 'Coffee', path: '/moyu-tools', color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)' },
  { title: '开发工具', icon: 'Compass', path: '/dev-tools', color: 'linear-gradient(135deg, #ff9a9e, #fecfef)' },
  { title: '版本历史', icon: 'DataAnalysis', path: '/version-history', color: 'linear-gradient(135deg, #89f7fe, #66a6ff)' }
])
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
  overflow-x: hidden;
}

/* Hero 区域 */
.hero-section {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
  animation: float 15s infinite ease-in-out;
}

.shape-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 300px;
  height: 300px;
  top: 50%;
  right: -50px;
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.3), rgba(0, 242, 254, 0.3));
  animation-delay: -5s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  bottom: 10%;
  left: 10%;
  background: linear-gradient(135deg, rgba(250, 112, 154, 0.3), rgba(254, 225, 64, 0.3));
  animation-delay: -10s;
}

.shape-4 {
  width: 150px;
  height: 150px;
  top: 20%;
  right: 20%;
  background: linear-gradient(135deg, rgba(67, 233, 123, 0.3), rgba(56, 249, 215, 0.3));
  animation-delay: -7s;
}

.shape-5 {
  width: 250px;
  height: 250px;
  bottom: -50px;
  right: 30%;
  background: linear-gradient(135deg, rgba(161, 140, 209, 0.3), rgba(251, 194, 235, 0.3));
  animation-delay: -12s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(30px, -30px) rotate(5deg);
  }
  50% {
    transform: translate(-20px, 20px) rotate(-5deg);
  }
  75% {
    transform: translate(20px, 10px) rotate(3deg);
  }
}

.hero-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 800px;
  animation: fadeInUp 1s ease-out;
}

.hero-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50px;
  color: #a5b4fc;
  font-size: 14px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.hero-title {
  margin: 0 0 24px;
  line-height: 1.2;
}

.gradient-text {
  font-size: 72px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 30%, #f093fb 60%, #4facfe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -2px;
}

.sub-title {
  font-size: 28px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
  letter-spacing: 2px;
}

.hero-desc {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.8;
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 60px;
}

.action-btn {
  padding: 14px 32px;
  font-size: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.5);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: white;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

/* 功能区域 */
.features-section {
  padding: 100px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 16px;
}

.section-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out both;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.feature-card:hover::before {
  opacity: 1;
}

.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 20px;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 12px;
}

.feature-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

.feature-arrow {
  position: absolute;
  right: 24px;
  bottom: 24px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-arrow {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateX(4px);
}

/* 快速入口区域 */
.quick-access-section {
  padding: 80px 20px;
  background: white;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-item:hover {
  background: #f8fafc;
  transform: translateY(-4px);
}

.quick-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: transform 0.3s ease;
}

.quick-item:hover .quick-icon {
  transform: scale(1.1);
}

.quick-title {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

/* 亮点区域 */
.highlights-section {
  padding: 80px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.highlights-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.highlight-item {
  display: flex;
  gap: 20px;
  padding: 32px;
  background: white;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.highlight-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

.highlight-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.highlight-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.highlight-info p {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

/* CTA 区域 */
.cta-section {
  padding: 100px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  text-align: center;
}

.cta-content h2 {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin: 0 0 16px;
}

.cta-content p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 40px;
}

.cta-btn {
  padding: 16px 40px;
  font-size: 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.5);
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .quick-access-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .highlights-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .gradient-text {
    font-size: 48px;
  }
  
  .sub-title {
    font-size: 20px;
  }
  
  .hero-desc {
    font-size: 16px;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .hero-stats {
    flex-wrap: wrap;
    gap: 24px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-access-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  
  .section-title {
    font-size: 28px;
  }
  
  .cta-content h2 {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .gradient-text {
    font-size: 36px;
  }
  
  .quick-access-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
