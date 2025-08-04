import { createRouter, createWebHistory } from 'vue-router'

const Home = () => import('../views/Home.vue')
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Profile = () => import('../views/Profile.vue')
const AnnouncementList = () => import('../views/AnnouncementList.vue')
const Study = () => import('../views/Study.vue')
const RushSelection = () => import('../views/RushSelection.vue')
const DormSelection = () => import('../views/DormSelection.vue')
const CourseSelection = () => import('../views/CourseSelection.vue')
const MyClasses = () => import('../views/MyClasses.vue')
const GroupDetail = () => import('../views/GroupDetail.vue')
const AIAssistant = () => import('../views/AIAssistant.vue')
const Forum = () => import('../views/Forum.vue')
const PostDetail = () => import('../views/PostDetail.vue')
const WordLearning = () => import('../views/WordLearning.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { hideChrome: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { hideChrome: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true },
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: AnnouncementList,
    meta: { requiresAuth: true },
  },
  {
    path: '/study',
    name: 'Study',
    component: Study,
    meta: { requiresAuth: true },
  },
  {
    path: '/rush-selection',
    name: 'RushSelection',
    component: RushSelection,
    meta: { requiresAuth: true },
  },
  {
    path: '/dorm-selection',
    name: 'DormSelection',
    component: DormSelection,
    meta: { requiresAuth: true },
  },
  {
    path: '/course-selection',
    name: 'CourseSelection',
    component: CourseSelection,
    meta: { requiresAuth: true },
  },
  {
    path: '/my-classes',
    name: 'MyClasses',
    component: MyClasses,
    meta: { requiresAuth: true },
  },
  {
    path: '/group/detail',
    name: 'GroupDetail',
    component: GroupDetail,
    meta: { requiresAuth: true },
  },
  {
    path: '/ai-assistant',
    name: 'AIAssistant',
    component: AIAssistant,
    meta: { requiresAuth: true },
  },
  {
    path: '/word-learning',
    name: 'WordLearning',
    component: WordLearning,
    meta: { requiresAuth: true },
  },
  {
    path: '/forum',
    name: 'Forum',
    component: Forum,
    meta: { requiresAuth: true },
  },
  {
    path: '/post/detail/:id',
    name: 'PostDetail',
    component: PostDetail,
    meta: { requiresAuth: true },
    props:true,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  const tokenValue = localStorage.getItem('tokenValue')
  console.log('Navigation guard:', to.path, 'Token exists:', !!tokenValue)
  const isAuth = !!tokenValue

  if (to.meta.requiresAuth && !isAuth) {
    // 未登录访问受保护页面，跳转登录
    return next({ path: '/login', replace: true })
  }

  if (isAuth && (to.path === '/login' || to.path === '/register')) {
    // 已登录访问登录/注册页，直接跳首页
    return next({ path: '/', replace: true })
  }

  next()
})

export default router 