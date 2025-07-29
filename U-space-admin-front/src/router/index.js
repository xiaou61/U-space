import { createRouter, createWebHistory } from 'vue-router'
import BlankRouter from '../layouts/BlankRouter.vue'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'class',
        name: 'ClassManagement',
        component: () => import('../views/ClassList.vue')
      },
      {
        path: 'teacher',
        name: 'TeacherManagement',
        component: () => import('../views/TeacherList.vue')
      },
      {
        path: 'course',
        component: BlankRouter,
        children:[
          { path: 'management', name: 'CourseManagement', component: () => import('../views/CourseList.vue') }
        ]
      },
      {
        path: 'student',
        component: BlankRouter,
        children:[
          { path: 'unaudited', name:'StudentUnaudited', component: () => import('../views/StudentUnaudited.vue') },
          { path: 'all', name:'StudentAll', component: () => import('../views/StudentAll.vue') }
        ]
      },
      {
        path: 'group',
        name: 'GroupManagement',
        component: () => import('../views/GroupList.vue')
      },
      {
        path: 'operlog',
        name: 'OperLog',
        component: () => import('../views/OperLog.vue')
      },
      {
        path: 'file',
        name: 'FileManage',
        component: () => import('../views/FileManage.vue')
      },
      {
        path: 'online',
        name: 'OnlineUser',
        component: () => import('../views/OnlineUser.vue')
      },
      {
        path: 'announcement',
        name: 'AnnouncementManagement',
        component: () => import('../views/AnnouncementList.vue')
      },
      {
        path: 'signin',
        name: 'SigninManagement',
        component: () => import('../views/SigninList.vue')
      },
      {
        path: 'homework',
        name: 'HomeworkManagement',
        component: () => import('../views/HomeworkList.vue')
      },
      {
        path: 'homework/:id',
        name: 'HomeworkDetail',
        component: () => import('../views/HomeworkDetail.vue'),
        props: true
      },
      {
        path: 'ai',
        name: 'AiChatManagement',
        component: () => import('../views/AiChatList.vue')
      },
      {
        path: 'material',
        name: 'MaterialManagement',
        component: () => import('../views/MaterialList.vue')
      },
      {
        path: 'schoolinfo',
        name: 'SchoolInfoManagement',
        component: () => import('../views/SchoolInfo.vue')
      },
      {
        path: 'bbs',
        name: 'BbsAdminManagement',
        component: () => import('../views/BbsAdmin.vue')
      },
      {
        path: 'bbs-category',
        name: 'BbsCategoryManagement',
        component: () => import('../views/BbsCategory.vue')
      },
      {
        path: 'bbs-sensitive',
        name: 'BbsSensitiveManagement',
        component: () => import('../views/BbsSensitive.vue')
      },
      {
        path: 'dorm',
        component: BlankRouter,
        children:[
          { path: 'building', name:'DormBuildingManagement', component: () => import('../views/DormBuilding.vue') },
          { path: 'room',     name:'DormRoomManagement',     component: () => import('../views/DormRoom.vue') },
          { path: 'bed',      name:'DormBedManagement',      component: () => import('../views/DormBed.vue') }
        ]
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：未登录则跳转到登录页
router.beforeEach((to, from, next) => {
  const tokenValue = localStorage.getItem('tokenValue')
  if (to.meta.requiresAuth && !tokenValue) {
    return next('/login')
  }
  // 已登录用户访问登录页时重定向到首页
  if (to.path === '/login' && tokenValue) {
    return next('/')
  }
  next()
})

export default router 