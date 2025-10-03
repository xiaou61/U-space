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
    path: '/community/users/:userId',
    name: 'CommunityUserProfile',
    component: () => import('@/views/community/UserProfile.vue'),
    meta: {
      title: '用户主页',
      requiresAuth: true,
      keepAlive: false // 不缓存，确保数据实时
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
    path: '/moments/user/:userId',
    name: 'MomentUserProfile',
    component: () => import('@/views/moments/UserProfile.vue'),
    meta: {
      title: '用户主页',
      requiresAuth: true,
      keepAlive: false
    }
  },
  {
    path: '/moments/my-favorites',
    name: 'MomentFavorites',
    component: () => import('@/views/moments/MyFavorites.vue'),
    meta: {
      title: '我的收藏',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/Index.vue'),
    meta: {
      title: '聊天室',
      requiresAuth: true,
      keepAlive: false
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
    path: '/points',
    name: 'Points',
    component: () => import('@/views/points/Index.vue'),
    meta: {
      title: '我的积分',
      requiresAuth: true,
      keepAlive: true
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
    path: '/dev-tools',
    name: 'DevTools',
    component: () => import('@/views/dev-tools/index.vue'),
    meta: {
      title: '程序员工具',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/dev-tools/json',
    name: 'JsonTool',
    component: () => import('@/views/dev-tools/JsonTool.vue'),
    meta: {
      title: 'JSON工具',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/dev-tools/text-diff',
    name: 'TextDiff',
    component: () => import('@/views/dev-tools/TextDiff.vue'),
    meta: {
      title: '文本比对',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/dev-tools/translation',
    name: 'Translation',
    component: () => import('@/views/dev-tools/Translation.vue'),
    meta: {
      title: '聚合翻译',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/moyu-tools',
    name: 'MoyuTools',
    component: () => import('@/views/moyu-tools/index.vue'),
    meta: {
      title: '摸鱼工具',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/moyu-tools/hot-topics',
    name: 'HotTopics',
    component: () => import('@/views/moyu-tools/HotTopics.vue'),
    meta: {
      title: '今日热榜',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/moyu-tools/salary-calculator',
    name: 'SalaryCalculator',
    component: () => import('@/views/moyu-tools/SalaryCalculator.vue'),
    meta: {
      title: '时薪计算器',
      requiresAuth: true,
      keepAlive: false
    }
  },
  {
    path: '/moyu-tools/calendar',
    name: 'DeveloperCalendar',
    component: () => import('@/views/moyu-tools/DeveloperCalendar.vue'),
    meta: {
      title: '程序员日历',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/moyu-tools/daily-content',
    name: 'DailyContent',
    component: () => import('@/views/moyu-tools/DailyContent.vue'),
    meta: {
      title: '每日内容',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/moyu-tools/bug-store',
    name: 'BugStore',
    component: () => import('@/views/moyu-tools/bug-store.vue'),
    meta: {
      title: 'Bug商店',
      requiresAuth: true,
      keepAlive: false
    }
  },
  {
    path: '/blog',
    name: 'MyBlog',
    component: () => import('@/views/blog/Index.vue'),
    meta: {
      title: '我的博客',
      requiresAuth: true,
      keepAlive: true
    }
  },
  {
    path: '/blog/editor',
    name: 'BlogEditor',
    component: () => import('@/views/blog/Editor.vue'),
    meta: {
      title: '写文章',
      requiresAuth: true,
      keepAlive: false
    }
  },
  {
    path: '/blog/editor/:id',
    name: 'BlogEditorEdit',
    component: () => import('@/views/blog/Editor.vue'),
    meta: {
      title: '编辑文章',
      requiresAuth: true,
      keepAlive: false
    }
  },
  {
    path: '/blog/:userId',
    name: 'BlogHome',
    component: () => import('@/views/blog/BlogHome.vue'),
    meta: {
      title: '博客主页',
      requiresAuth: false,
      keepAlive: true
    }
  },
  {
    path: '/blog/:userId/article/:articleId',
    name: 'ArticleDetail',
    component: () => import('@/views/blog/ArticleDetail.vue'),
    meta: {
      title: '文章详情',
      requiresAuth: false,
      keepAlive: false
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
    if (!userStore.token || !userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
  }
  
  // 如果已登录，访问登录或注册页面时跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.token && userStore.isLoggedIn) {
    next('/')
    return
  }
  
  next()
})

export default router 