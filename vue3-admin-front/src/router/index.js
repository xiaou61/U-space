import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
    },
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表板',
          icon: 'Odometer',
        },
      },
      {
        path: 'logs',
        name: 'Logs',
        meta: {
          title: '日志管理',
          icon: 'Document',
        },
        children: [
          {
            path: 'login',
            name: 'LoginLogs',
            component: () => import('@/views/logs/login/index.vue'),
            meta: {
              title: '登录日志',
              icon: 'UserFilled',
            },
          },
          {
            path: 'operation',
            name: 'OperationLogs',
            component: () => import('@/views/logs/operation/index.vue'),
            meta: {
              title: '操作日志',
              icon: 'Operation',
            },
          },
        ],
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: '个人中心',
          hideInMenu: true,
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
    },
  },
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - Code-Nest 管理后台` : 'Code-Nest 管理后台'
  
  // 如果访问登录页
  if (to.path === '/login') {
    if (isLoggedIn) {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 检查是否需要登录
  if (!isLoggedIn) {
    next('/login')
    return
  }
  
  // 检查用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      next('/login')
      return
    }
  }
  
  next()
})

// 全局后置守卫
router.afterEach(() => {
  NProgress.done()
})

export default router 