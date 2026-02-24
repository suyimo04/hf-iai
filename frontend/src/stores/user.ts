import { defineStore } from 'pinia'
import type { User, Role } from '@/types'
import type { ApiResponse, LoginResponse } from '@/types/api'
import { getToken, setToken, removeToken } from '@/utils/auth'
import request from '@/utils/request'

// 菜单项类型
export interface MenuItem {
  id: number
  parentId: number
  name: string
  path: string
  component: string
  icon?: string
  sortOrder: number
  visible: boolean
  children?: MenuItem[]
}

interface UserState {
  token: string | null
  user: User | null
  permissions: string[]
  menus: MenuItem[]
}

// 构建菜单树
function buildMenuTree(menus: MenuItem[]): MenuItem[] {
  const menuMap = new Map<number, MenuItem>()
  const tree: MenuItem[] = []

  // 先创建所有菜单的映射
  menus.forEach(menu => {
    menuMap.set(menu.id, { ...menu, children: [] })
  })

  // 构建树结构
  menus.forEach(menu => {
    const node = menuMap.get(menu.id)!
    if (menu.parentId === 0 || !menuMap.has(menu.parentId)) {
      tree.push(node)
    } else {
      const parent = menuMap.get(menu.parentId)!
      parent.children = parent.children || []
      parent.children.push(node)
    }
  })

  // 按sortOrder排序
  const sortMenus = (items: MenuItem[]) => {
    items.sort((a, b) => a.sortOrder - b.sortOrder)
    items.forEach(item => {
      if (item.children?.length) {
        sortMenus(item.children)
      }
    })
  }
  sortMenus(tree)

  return tree
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    user: null,
    permissions: [],
    menus: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => state.user?.role,
    menuTree: (state) => buildMenuTree(state.menus),
    // 保留原有的基于角色的权限检查
    hasRolePermission: (state) => {
      return (roles: Role[]) => {
        if (!state.user?.role) return false
        return roles.includes(state.user.role)
      }
    }
  },

  actions: {
    // 检查是否拥有指定权限码
    hasPermission(code: string): boolean {
      return this.permissions.includes(code)
    },

    // 设置权限列表
    setPermissions(permissions: string[]) {
      this.permissions = permissions
    },

    // 设置菜单列表
    setMenus(menus: MenuItem[]) {
      this.menus = menus
    },

    // 清除用户信息
    clearUserInfo() {
      this.token = null
      this.user = null
      this.permissions = []
      this.menus = []
      removeToken()
    },

    async login(username: string, password: string) {
      const response = await request.post<ApiResponse<LoginResponse>>('/auth/login', {
        username,
        password
      })
      const { token, user } = response.data.data
      this.token = token
      this.user = user
      setToken(token)

      // 登录成功后获取权限和菜单
      await this.fetchPermissionsAndMenus()

      return user
    },

    logout() {
      this.clearUserInfo()
    },

    async fetchUserInfo() {
      const response = await request.get<ApiResponse<User>>('/auth/me')
      this.user = response.data.data
      return this.user
    },

    // 获取用户权限和菜单
    async fetchPermissionsAndMenus() {
      try {
        const [permRes, menuRes] = await Promise.all([
          request.get<ApiResponse<string[]>>('/auth/permissions'),
          request.get<ApiResponse<MenuItem[]>>('/auth/menus')
        ])
        this.permissions = permRes.data.data || []
        this.menus = menuRes.data.data || []
      } catch (error) {
        console.error('获取权限和菜单失败:', error)
        // 失败时不阻塞，使用空数组
        this.permissions = []
        this.menus = []
      }
    },

    setUser(user: User) {
      this.user = user
    }
  }
})
