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
      requiresAuth: true
    }
  },
  {
    path: '/interview/question-sets/:id',
    name: 'QuestionSetDetail',
    component: () => import('@/views/interview/QuestionSetDetail.vue'),
    meta: {
      title: '题单详情',
      requiresAuth: true
    }
  },
  {
    path: '/interview/questions/:setId/:questionId',
    name: 'QuestionDetail',
    component: () => import('@/views/interview/QuestionDetail.vue'),
    meta: {
      title: '题目学习',
      requiresAuth: true
    }
  },
  {
    path: '/interview/favorites',
    name: 'MyFavorites',
    component: () => import('@/views/interview/Favorites.vue'),
    meta: {
      title: '我的收藏',
      requiresAuth: true
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