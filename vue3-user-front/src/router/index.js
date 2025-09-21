import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: '首页',
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/interview',
    name: 'Interview',
    component: () => import('@/views/interview/Index.vue'),
    meta: {
      title: '面试题库',
      requiresAuth: true,
      keepAlive: true // 开启页面缓存
    }
  },
  {
    path: '/interview/random',
    name: 'RandomQuestions',
    component: () => import('@/views/interview/RandomQuestions.vue'),
    meta: {
      title: '随机抽题',
      requiresAuth: true,
      keepAlive: false // 随机抽题页面不缓存，确保每次都是新的状态
    }
  },
  {
    path: '/interview/question-sets/:id',
    name: 'QuestionSetDetail',
    component: () => import('@/views/interview/QuestionSetDetail.vue'),
    meta: {
      title: '题单详情',
      requiresAuth: true,
      keepAlive: true // 开启页面缓存
    }
  },
  {
    path: '/interview/questions/:setId/:questionId',
    name: 'QuestionDetail',
    component: () => import('@/views/interview/QuestionDetail.vue'),
    meta: {
      title: '题目学习',
      requiresAuth: true,
      keepAlive: false // 题目页面不缓存，确保学习进度正确
    }
  },
  {
    path: '/interview/favorites',
    name: 'MyFavorites',
    component: () => import('@/views/interview/Favorites.vue'),
    meta: {
      title: '我的收藏',
      requiresAuth: true,
      keepAlive: true // 开启页面缓存
    }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/community/Index.vue'),
    meta: {
      title: '技术社区',
      requiresAuth: true,
      keepAlive: true // 开启页面缓存
    }
  },
  {
    path: '/community/posts/:id',
    name: 'PostDetail',
    component: () => import('@/views/community/PostDetail.vue'),
    meta: {
      title: '帖子详情',
      requiresAuth: true,
      keepAlive: false // 帖子详情不缓存，确保数据实时
    }
  },
  {
    path: '/community/collections',
    name: 'MyCollections',
    component: () => import('@/views/community/Collections.vue'),
    meta: {
      title: '我的收藏',
      requiresAuth: true,
      keepAlive: true // 开启页面缓存
    }
  },
  {
    path: '/community/my-posts',
    name: 'MyPosts',
    component: () => import('@/views/community/MyPosts.vue'),
    meta: {
      title: '我的帖子',
      requiresAuth: true,
      keepAlive: true // 开启页面缓存
    }
  },
  {
    path: '/community/create',
    name: 'CreatePost',
    component: () => import('@/views/community/CreatePost.vue'),
    meta: {
      title: '创作帖子',
      requiresAuth: true,
      keepAlive: false // 不缓存，确保每次都是新的编辑状态
    }
  },
  {
    path: '/notification',
    name: 'Notification',
    component: () => import('@/views/notification/index.vue'),
    meta: {
      title: '通知中心',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/moments',
    name: 'Moments',
    component: () => import('@/views/moments/Index.vue'),
    meta: {
      title: '朋友圈',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: {
      title: '个人中心',
      requiresAuth: true
    }
  },
  {
    path: '/knowledge',
    name: 'Knowledge',
    component: () => import('@/views/knowledge/Index.vue'),
    meta: {
      title: '知识图谱',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/knowledge/maps/:id',
    name: 'KnowledgeMapViewer',
    component: () => import('@/views/knowledge/MapViewer.vue'),
    props: true,
    meta: {
      title: '知识图谱学习',
      requiresAuth: true,
      keepAlive: false
    }
  },
  {
    path: '/version-history',
    name: 'VersionHistory',
    component: () => import('@/views/version/index.vue'),
    meta: {
      title: '版本更新历史',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = `${to.meta.title || '用户端'} - Code Nest`
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLogin()) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
  }
  
  // 如果已登录，访问登录或注册页面时跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLogin()) {
    next('/')
    return
  }
  
  next()
})

export default router 