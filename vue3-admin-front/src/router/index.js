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
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'Avatar',
        },
      },
      {
        path: 'interview',
        name: 'InterviewManagement',
        meta: {
          title: '面试题管理',
          icon: 'Document',
        },
        children: [
          {
            path: 'categories',
            name: 'InterviewCategories',
            component: () => import('@/views/interview/categories/index.vue'),
            meta: {
              title: '分类管理',
              icon: 'FolderOpened',
            },
          },
          {
            path: 'question-sets',
            name: 'InterviewQuestionSets',
            component: () => import('@/views/interview/question-sets/index.vue'),
            meta: {
              title: '题单管理',
              icon: 'Collection',
            },
          },
          {
            path: 'questions',
            name: 'InterviewQuestions',
            component: () => import('@/views/interview/questions/index.vue'),
            meta: {
              title: '题目管理',
              icon: 'Edit',
            },
          },
        ],
      },
      {
        path: 'community',
        name: 'CommunityManagement',
        meta: {
          title: '社区管理',
          icon: 'ChatDotRound',
        },
        children: [
          {
            path: 'posts',
            name: 'CommunityPosts',
            component: () => import('@/views/community/posts/index.vue'),
            meta: {
              title: '帖子管理',
              icon: 'Document',
            },
          },
          {
            path: 'comments',
            name: 'CommunityComments',
            component: () => import('@/views/community/comments/index.vue'),
            meta: {
              title: '评论管理',
              icon: 'ChatLineRound',
            },
          },
          {
            path: 'users',
            name: 'CommunityUsers',
            component: () => import('@/views/community/users/index.vue'),
            meta: {
              title: '用户管理',
              icon: 'User',
            },
          },
          {
            path: 'categories',
            name: 'CommunityCategories',
            component: () => import('@/views/community/categories/index.vue'),
            meta: {
              title: '分类管理',
              icon: 'Files',
            },
          },
        ],
      },
      {
        path: 'filestorage',
        name: 'FileStorageManagement',
        meta: {
          title: '文件存储管理',
          icon: 'FolderOpened',
        },
        children: [
          {
            path: 'storage-config',
            name: 'StorageConfig',
            component: () => import('@/views/filestorage/storage-config/index.vue'),
            meta: {
              title: '存储配置',
              icon: 'SetUp',
            },
          },
          {
            path: 'file-management',
            name: 'FileManagement',
            component: () => import('@/views/filestorage/file-management/index.vue'),
            meta: {
              title: '文件管理',
              icon: 'Document',
            },
          },
          {
            path: 'migration',
            name: 'FileMigration',
            component: () => import('@/views/filestorage/migration/index.vue'),
            meta: {
              title: '文件迁移',
              icon: 'Sort',
            },
          },
          {
            path: 'system-settings',
            name: 'FileSystemSettings',
            component: () => import('@/views/filestorage/system-settings/index.vue'),
            meta: {
              title: '系统设置',
              icon: 'Tools',
            },
          },
        ],
      },
      {
        path: 'system',
        name: 'SystemManagement',
        meta: {
          title: '系统管理',
          icon: 'Setting',
        },
        children: [
          {
            path: 'monitor',
            name: 'SystemMonitor',
            meta: {
              title: '系统监控',
              icon: 'Monitor',
            },
            children: [
              {
                path: 'sql',
                name: 'SqlMonitor',
                component: () => import('@/views/system/monitor/SqlMonitor.vue'),
                meta: {
                  title: 'SQL监控',
                  icon: 'DataAnalysis',
                },
              },
            ],
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
      // 已登录用户访问登录页，重定向到首页
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
  
  // 检查并刷新token（如果需要）
  try {
    const tokenValid = await userStore.checkAndRefreshToken()
    if (!tokenValid) {
      next('/login')
      return
    }
  } catch (error) {
    console.error('Token检查失败:', error)
    next('/login')
    return
  }
  
  // 检查用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 获取用户信息失败，清除token并跳转登录页
      userStore.clearUserData()
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