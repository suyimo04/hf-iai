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
    path: '/public/questionnaire/:token',
    name: 'PublicQuestionnaire',
    component: () => import('@/views/questionnaire/PublicQuestionnaireForm.vue'),
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
        path: 'members/flow',
        name: 'MemberFlow',
        component: () => import('@/views/salary/MemberFlowLogView.vue'),
        meta: { title: '成员流转', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'recruitment',
        name: 'Recruitment',
        component: () => import('@/views/recruitment/index.vue'),
        meta: { title: '招募管理', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'recruitment/interview-config',
        name: 'InterviewConfig',
        component: () => import('@/views/interview/config/index.vue'),
        meta: { title: '面试配置', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'interview',
        name: 'Interview',
        component: () => import('@/views/interview/InterviewListPage.vue'),
        meta: { title: 'AI面试列表', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'interview/replay/:id',
        name: 'InterviewReplay',
        component: () => import('@/views/interview/InterviewReplay.vue'),
        meta: { title: '面试回放', roles: ['ADMIN', 'LEADER'] }
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
        path: 'salary/batch',
        name: 'SalaryBatchEdit',
        component: () => import('@/views/salary/SalaryBatchEdit.vue'),
        meta: { title: '薪酬批量编辑', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'salary/flow',
        name: 'SalaryFlowLogs',
        component: () => import('@/views/salary/MemberFlowLogView.vue'),
        meta: { title: '成员流转日志', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'questionnaire',
        name: 'QuestionnaireList',
        component: () => import('@/views/questionnaire/QuestionnaireList.vue'),
        meta: { title: '问卷列表', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'questionnaire/create',
        name: 'QuestionnaireCreate',
        component: () => import('@/views/questionnaire/QuestionnaireDesigner.vue'),
        meta: { title: '创建问卷', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'questionnaire/edit/:id',
        name: 'QuestionnaireEdit',
        component: () => import('@/views/questionnaire/QuestionnaireDesigner.vue'),
        meta: { title: '编辑问卷', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'questionnaire/preview/:id',
        name: 'QuestionnairePreview',
        component: () => import('@/views/questionnaire/QuestionnairePreview.vue'),
        meta: { title: '问卷预览', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'questionnaire/:id/responses',
        name: 'QuestionnaireResponses',
        component: () => import('@/views/questionnaire/ResponseList.vue'),
        meta: { title: '问卷回复', roles: ['ADMIN', 'LEADER', 'VICE_LEADER'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '系统配置', roles: ['ADMIN'] }
      },
      {
        path: 'settings/config',
        name: 'ConfigCenter',
        component: () => import('@/views/settings/ConfigCenter.vue'),
        meta: { title: '配置中心', roles: ['ADMIN'] }
      },
      {
        path: 'settings/menus',
        name: 'MenuManager',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '菜单管理', roles: ['ADMIN'] }
      },
      {
        path: 'settings/permissions',
        name: 'PermissionManager',
        component: () => import('@/views/settings/PermissionManager.vue'),
        meta: { title: '权限管理', roles: ['ADMIN'] }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/logs/index.vue'),
        meta: { title: '操作日志', roles: ['ADMIN', 'LEADER'] }
      },
      {
        path: 'logs/role-change',
        name: 'RoleChangeLogs',
        component: () => import('@/views/logs/RoleChangeLogView.vue'),
        meta: { title: '角色变更日志', roles: ['ADMIN', 'LEADER'] }
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
