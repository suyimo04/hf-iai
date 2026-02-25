import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuards } from './guards'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/apply',
    name: 'Apply',
    component: () => import('@/views/application/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('@/views/members/index.vue'),
        meta: { title: '成员管理', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'recruitment',
        name: 'Recruitment',
        component: () => import('@/views/recruitment/index.vue'),
        meta: { title: '招募管理', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'interview',
        name: 'Interview',
        component: () => import('@/views/interview/index.vue'),
        meta: { title: 'AI面试', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'interview/config',
        name: 'InterviewConfig',
        component: () => import('@/views/interview/config/index.vue'),
        meta: { title: '面试配置', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/activities/index.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'salary',
        name: 'Salary',
        component: () => import('@/views/salary/index.vue'),
        meta: { title: '薪酬管理', roles: ['ADMIN', 'LEADER', 'VICE_LEADER', 'MEMBER'] }
      },
      {
        path: 'member-flow-logs',
        name: 'MemberFlowLogs',
        component: () => import('@/views/salary/MemberFlowLogView.vue'),
        meta: { title: '成员流转记录', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '系统配置', roles: ['ADMIN'] }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/logs/index.vue'),
        meta: { title: '操作日志', roles: ['ADMIN', 'LEADER'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

setupRouterGuards(router)

export default router
