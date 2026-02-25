import type { RouteRecordRaw, Router } from 'vue-router'
import type { Role } from '@/types'

/**
 * 菜单项接口 - 对应后端返回的菜单数据结构
 */
export interface MenuItem {
  id: number
  parentId: number
  name: string
  path: string
  component: string
  icon?: string
  sortOrder: number
  visible: boolean
  roles?: Role[]
  children?: MenuItem[]
}

/**
 * 动态导入视图组件
 * 使用 import.meta.glob 实现懒加载
 */
const viewModules = import.meta.glob('../views/**/*.vue')

/**
 * 根据组件路径获取组件
 * @param componentPath 组件相对路径 (如 'dashboard/index' 或 'members/index')
 */
function resolveComponent(componentPath: string) {
  if (!componentPath) return undefined

  // 标准化路径格式
  const normalizedPath = componentPath.startsWith('/')
    ? componentPath.slice(1)
    : componentPath

  // 尝试多种路径格式匹配
  const possiblePaths = [
    `../views/${normalizedPath}.vue`,
    `../views/${normalizedPath}/index.vue`,
  ]

  for (const path of possiblePaths) {
    if (viewModules[path]) {
      return viewModules[path]
    }
  }

  console.warn(`[DynamicRouter] Component not found: ${componentPath}`)
  return undefined
}

/**
 * 将菜单数据转换为 Vue Router 路由配置
 * @param menus 菜单数据数组
 * @returns 路由配置数组
 */
export function generateRoutes(menus: MenuItem[]): RouteRecordRaw[] {
  return menus
    .filter(menu => menu.visible !== false)
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    .map(menu => {
      const route: RouteRecordRaw = {
        path: menu.path.startsWith('/') ? menu.path.slice(1) : menu.path,
        name: menu.name,
        meta: {
          title: menu.name,
          icon: menu.icon,
          roles: menu.roles,
          dynamic: true, // 标记为动态路由，便于后续清理
        },
        component: resolveComponent(menu.component),
      }

      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        route.children = generateRoutes(menu.children)
      }

      return route
    })
}

/**
 * 将动态路由添加到路由器
 * @param router Vue Router 实例
 * @param menus 菜单数据
 * @param parentName 父路由名称，默认为 undefined (添加到根布局)
 */
export function addDynamicRoutes(
  router: Router,
  menus: MenuItem[],
  parentName?: string
) {
  const routes = generateRoutes(menus)

  routes.forEach(route => {
    // 检查路由是否已存在
    if (router.hasRoute(route.name as string)) {
      router.removeRoute(route.name as string)
    }

    if (parentName) {
      router.addRoute(parentName, route)
    } else {
      // 默认添加到根路由的子路由（布局内）
      const layoutRoute = router.getRoutes().find(r => r.path === '/')
      if (layoutRoute && layoutRoute.name) {
        router.addRoute(layoutRoute.name, route)
      } else {
        router.addRoute(route)
      }
    }
  })

  return routes
}

/**
 * 重置动态路由（用于登出时清理）
 * @param router Vue Router 实例
 */
export function resetDynamicRoutes(router: Router) {
  const routes = router.getRoutes()

  routes.forEach(route => {
    // 只移除标记为动态的路由
    if (route.meta?.dynamic && route.name) {
      router.removeRoute(route.name)
    }
  })
}

/**
 * 检查路由是否存在
 * @param router Vue Router 实例
 * @param name 路由名称
 */
export function hasRoute(router: Router, name: string): boolean {
  return router.hasRoute(name)
}

/**
 * 获取所有动态路由
 * @param router Vue Router 实例
 */
export function getDynamicRoutes(router: Router): RouteRecordRaw[] {
  return router.getRoutes().filter(route => route.meta?.dynamic) as RouteRecordRaw[]
}
