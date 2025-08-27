import { 
  House, User, Notebook, VideoCamera, 
  Management, Files, Monitor, ChatDotRound, Avatar, UserFilled,
  Trophy
} from '@element-plus/icons-vue'

// 菜单权限配置
export const menuConfig = [
  {
    key: 'home',
    title: '首页',
    icon: House,
    path: '/',
    permission: null, // null表示所有用户都可以访问
  },
  {
    key: 'school',
    title: '学校教学管理',
    icon: User,
    permission: 'school_management',
    children: [
      {
        key: 'class',
        title: '班级管理',
        path: '/class',
        permission: 'class_management'
      },
      {
        key: 'teacher',
        title: '教师管理',
        path: '/teacher',
        permission: 'teacher_management'
      },
      {
        key: 'student',
        title: '学生信息管理',
        permission: 'student_management',
        children: [
          {
            key: 'student_unaudited',
            title: '未审批学生',
            path: '/student/unaudited',
            permission: 'student_unaudited'
          },
          {
            key: 'student_all',
            title: '全部学生',
            path: '/student/all',
            permission: 'student_all'
          }
        ]
      }
    ]
  },
  {
    key: 'system',
    title: '系统设置',
    icon: Notebook,
    permission: 'system_settings',
    children: [
      {
        key: 'operlog',
        title: '操作日志',
        path: '/operlog',
        permission: 'operation_log'
      },
      {
        key: 'file',
        title: '文件管理',
        path: '/file',
        permission: 'file_management'
      },
      {
        key: 'online',
        title: '在线用户',
        path: '/online',
        permission: 'online_users'
      },
      {
        key: 'announcement',
        title: '公告管理',
        path: '/announcement',
        permission: 'announcement_management'
      },
      {
        key: 'schoolinfo',
        title: '学校信息管理',
        path: '/schoolinfo',
        permission: 'school_info'
      },
      {
        key: 'bbs',
        title: 'BBS管理员',
        path: '/bbs',
        permission: 'bbs_admin_management'
      },
      {
        key: 'role-permission',
        title: '角色权限管理',
        path: '/role-permission',
        permission: 'role_permission_management'
      },
      {
        key: 'admin-user',
        title: '管理员用户管理',
        path: '/admin-user',
        permission: 'admin_user_management'
      },
      {
        key: 'menu-management',
        title: '菜单权限管理',
        path: '/menu-management',
        permission: 'menu_management'
      }
    ]
  },
  {
    key: 'dorm',
    title: '宿舍信息',
    icon: House,
    permission: 'dorm_management',
    children: [
      {
        key: 'dorm_building',
        title: '宿舍楼管理',
        path: '/dorm/building',
        permission: 'dorm_building'
      },
      {
        key: 'dorm_room',
        title: '宿舍房间管理',
        path: '/dorm/room',
        permission: 'dorm_room'
      },
      {
        key: 'dorm_bed',
        title: '床位管理',
        path: '/dorm/bed',
        permission: 'dorm_bed'
      }
    ]
  },
  {
    key: 'course',
    title: '课程相关',
    icon: Notebook,
    permission: 'course_management',
    children: [
      {
        key: 'course_management',
        title: '课程管理',
        path: '/course/management',
        permission: 'course_management'
      }
    ]
  },
  {
    key: 'ai',
    title: 'AI对话管理',
    icon: ChatDotRound,
    path: '/ai',
    permission: 'ai_management'
  },
  {
    key: 'word',
    title: '单词管理',
    icon: Notebook,
    path: '/word',
    permission: 'word_management'
  },
  {
    key: 'video',
    title: '入学必看视频管理',
    icon: VideoCamera,
    path: '/video',
    permission: 'video_management'
  },
  {
    key: 'group',
    title: '群组管理',
    icon: User,
    path: '/group',
    permission: 'group_management'
  },
  {
    key: 'signin',
    title: '签到管理',
    icon: Notebook,
    path: '/signin',
    permission: 'signin_management'
  },
  {
    key: 'homework',
    title: '作业管理',
    icon: Notebook,
    path: '/homework',
    permission: 'homework_management'
  },
  {
    key: 'material',
    title: '资料管理',
    icon: Files,
    path: '/material',
    permission: 'material_management'
  },
  {
    key: 'bbs-category',
    title: '校园论坛分类管理',
    icon: Management,
    path: '/bbs-category',
    permission: 'bbs_category_management'
  },
  {
    key: 'bbs-sensitive',
    title: '校园论坛敏感词',
    icon: ChatDotRound,
    path: '/bbs-sensitive',
    permission: 'bbs_sensitive_management'
  },
  {
    key: 'activity',
    title: '活动相关',
    icon: Trophy,
    permission: 'activity_management',
    children: [
      {
        key: 'vote_activity',
        title: '投票活动',
        path: '/activity/vote',
        permission: 'vote_activity_management'
      }
    ]
  }
]

// 权限检查函数
export function hasPermission(permission, userPermissions) {
  if (!permission) return true // 无权限要求，所有人都可访问
  return userPermissions.includes(permission)
}

// 过滤菜单函数
export function filterMenuByPermissions(menus, userPermissions) {
  return menus.filter(menu => {
    // 检查当前菜单项权限
    if (!hasPermission(menu.permission, userPermissions)) {
      return false
    }
    
    // 如果有子菜单，递归过滤
    if (menu.children) {
      menu.children = filterMenuByPermissions(menu.children, userPermissions)
      // 如果过滤后没有子菜单了，且当前菜单项本身没有路径，则隐藏
      if (menu.children.length === 0 && !menu.path) {
        return false
      }
    }
    
    return true
  })
}

// 根据角色获取默认权限
export function getDefaultPermissionsByRole(roles) {
  const permissionMap = {
    'admin': [
      'school_management', 'class_management', 'teacher_management', 
      'student_management', 'student_unaudited', 'student_all',
      'system_settings', 'operation_log', 'file_management', 
      'online_users', 'announcement_management', 'school_info', 
      'bbs_admin_management', 'role_permission_management', 'admin_user_management', 'menu_management',
      'dorm_management', 'dorm_building', 'dorm_room', 'dorm_bed',
      'course_management', 'ai_management', 'word_management', 'video_management',
      'activity_management', 'vote_activity_management'
    ],
    'teacher': [
      'group_management', 'signin_management', 'homework_management', 'material_management'
    ],
    'bbs_admin': [
      'bbs_category_management', 'bbs_sensitive_management'
    ]
  }
  
  let permissions = []
  roles.forEach(role => {
    if (permissionMap[role]) {
      permissions = permissions.concat(permissionMap[role])
    }
  })
  
  return [...new Set(permissions)] // 去重
} 