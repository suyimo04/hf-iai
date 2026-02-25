import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import type { Role } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    title?: string
    roles?: Role[]
  }
}

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const token = getToken()
    const userStore = useUserStore()

    // 公开页面直接放行
    if (to.meta.public) {
      // 已登录用户访问登录页，跳转首页
      if (token && to.name === 'Login') {
        next({ path: '/' })
        return
      }
      next()
      return
    }

    // 未登录跳转登录页
    if (!token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 已登录但未获取用户信息
    if (!userStore.user) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        // 获取用户信息失败，清除token并跳转登录
        await userStore.logout()
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    }

    // 检查角色权限
    const requiredRoles = to.meta.roles
    if (requiredRoles && requiredRoles.length > 0) {
      const hasPermission = userStore.hasPermission(requiredRoles)
      if (!hasPermission) {
        // 无权限跳转首页
        next({ path: '/dashboard' })
        return
      }
    }

    next()
  })

  router.afterEach((to) => {
    // 设置页面标题
    const title = to.meta.title
    document.title = title ? `${title} - 花粉俱乐部` : '花粉俱乐部管理系统'
  })
}
