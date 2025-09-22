import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表板' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    children: [
      {
        path: '',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },
  {
    path: '/interview',
    component: Layout,
    redirect: '/interview/categories',
    meta: { title: '面试题目管理' },
    children: [
      {
        path: 'categories',
        name: 'InterviewCategories',
        component: () => import('@/views/interview/categories/index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'question-sets',
        name: 'QuestionSets',
        component: () => import('@/views/interview/question-sets/index.vue'),
        meta: { title: '题单管理' }
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('@/views/interview/questions/index.vue'),
        meta: { title: '题目管理' }
      }
    ]
  },
  {
    path: '/knowledge',
    component: Layout,
    redirect: '/knowledge/maps',
    meta: { title: '知识图谱管理' },
    children: [
      {
        path: 'maps',
        name: 'KnowledgeMaps',
        component: () => import('@/views/knowledge/maps/index.vue'),
        meta: { title: '图谱管理' }
      },
      {
        path: 'maps/:id/edit',
        name: 'KnowledgeMapEdit',
        component: () => import('@/views/knowledge/maps/edit.vue'),
        meta: { title: '编辑图谱' },
        props: true
      }
    ]
  },
  {
    path: '/community',
    component: Layout,
    redirect: '/community/categories',
    meta: { title: '社区管理' },
    children: [
      {
        path: 'categories',
        name: 'CommunityCategories',
        component: () => import('@/views/community/categories/index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'posts',
        name: 'CommunityPosts',
        component: () => import('@/views/community/posts/index.vue'),
        meta: { title: '帖子管理' }
      },
      {
        path: 'comments',
        name: 'CommunityComments',
        component: () => import('@/views/community/comments/index.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'users',
        name: 'CommunityUsers',
        component: () => import('@/views/community/users/index.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },
  {
    path: '/moments',
    component: Layout,
    redirect: '/moments/list',
    meta: { title: '朋友圈管理' },
    children: [
      {
        path: 'list',
        name: 'MomentsList',
        component: () => import('@/views/moments/list/index.vue'),
        meta: { title: '动态管理' }
      },
      {
        path: 'comments',
        name: 'MomentsComments',
        component: () => import('@/views/moments/comments/index.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'statistics',
        name: 'MomentsStatistics',
        component: () => import('@/views/moments/statistics/index.vue'),
        meta: { title: '数据统计' }
      }
    ]
  },
  {
    path: '/logs',
    component: Layout,
    redirect: '/logs/login',
    meta: { title: '日志管理' },
    children: [
      {
        path: 'login',
        name: 'LoginLogs',
        component: () => import('@/views/logs/login/index.vue'),
        meta: { title: '登录日志' }
      },
      {
        path: 'operation',
        name: 'OperationLogs',
        component: () => import('@/views/logs/operation/index.vue'),
        meta: { title: '操作日志' }
      }
    ]
  },
  {
    path: '/notification',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Notification',
        component: () => import('@/views/notification/index.vue'),
        meta: { title: '通知管理' }
      }
    ]
  },
  {
    path: '/sensitive',
    component: Layout,
    redirect: '/sensitive/words',
    meta: { title: '敏感词管理' },
    children: [
      {
        path: 'words',
        name: 'SensitiveWords',
        component: () => import('@/views/sensitive/words/index.vue'),
        meta: { title: '敏感词管理' }
      }
    ]
  },
  {
    path: '/filestorage',
    component: Layout,
    redirect: '/filestorage/storage-config',
    meta: { title: '文件存储管理' },
    children: [
      {
        path: 'storage-config',
        name: 'StorageConfig',
        component: () => import('@/views/filestorage/storage-config/index.vue'),
        meta: { title: '存储配置' }
      },
      {
        path: 'file-management',
        name: 'FileManagement',
        component: () => import('@/views/filestorage/file-management/index.vue'),
        meta: { title: '文件管理' }
      },
      {
        path: 'migration',
        name: 'FileMigration',
        component: () => import('@/views/filestorage/migration/index.vue'),
        meta: { title: '文件迁移' }
      },
      {
        path: 'system-settings',
        name: 'SystemSettings',
        component: () => import('@/views/filestorage/system-settings/index.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/monitor/sql',
    meta: { title: '系统管理' },
    children: [
      {
        path: 'monitor/sql',
        name: 'SqlMonitor',
        component: () => import('@/views/system/monitor/SqlMonitor.vue'),
        meta: { title: 'SQL监控' }
      },
      {
        path: 'version',
        name: 'VersionManagement',
        component: () => import('@/views/system/version/index.vue'),
        meta: { title: '版本管理' }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    redirect: '/profile/index',
    children: [
      {
        path: 'index',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'edit',
        name: 'EditProfile',
        component: () => import('@/views/profile/EditProfile.vue'),
        meta: { title: '编辑资料' }
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('@/views/profile/ChangePassword.vue'),
        meta: { title: '修改密码' }
      }
    ]
  },
  {
    path: '/moyu',
    component: Layout,
    redirect: '/moyu/calendar-events',
    meta: { title: '摸鱼工具管理' },
    children: [
      {
        path: 'calendar-events',
        name: 'MoyuCalendarEvents',
        component: () => import('@/views/moyu/calendar-events/index.vue'),
        meta: { title: '日历事件管理' }
      },
      {
        path: 'daily-content',
        name: 'MoyuDailyContent',
        component: () => import('@/views/moyu/daily-content/index.vue'),
        meta: { title: '每日内容管理' }
      },
      {
        path: 'statistics',
        name: 'MoyuStatistics',
        component: () => import('@/views/moyu/statistics/index.vue'),
        meta: { title: '统计分析' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - Code Nest 管理后台`
  }
  
  next()
})

export default router 