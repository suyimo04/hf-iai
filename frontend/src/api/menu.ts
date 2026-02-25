import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'

// 菜单
export interface Menu {
  id: number
  parentId?: number
  name: string
  path: string
  component?: string
  icon?: string
  sortOrder: number
  visible: boolean
  status: boolean
  createdAt?: string
  children?: Menu[]
}

// 创建菜单请求
export interface MenuCreateRequest {
  parentId?: number
  name: string
  path: string
  component?: string
  icon?: string
  sortOrder?: number
  visible?: boolean
}

// 更新菜单请求
export interface MenuUpdateRequest {
  name?: string
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  visible?: boolean
  status?: boolean
}

// 角色菜单分配请求
export interface RoleMenuAssignRequest {
  role: string
  menuIds: number[]
}

// 获取菜单树
export function getMenuTree() {
  return request.get<ApiResponse<Menu[]>>('/menus/tree')
}

// 获取所有菜单（平铺）
export function getAllMenus() {
  return request.get<ApiResponse<Menu[]>>('/menus')
}

// 获取角色的菜单列表
export function getRoleMenus(role: string) {
  return request.get<ApiResponse<Menu[]>>(`/menus/role/${role}`)
}

// 创建菜单
export function createMenu(data: MenuCreateRequest) {
  return request.post<ApiResponse<Menu>>('/menus', data)
}

// 更新菜单
export function updateMenu(id: number, data: MenuUpdateRequest) {
  return request.put<ApiResponse<Menu>>(`/menus/${id}`, data)
}

// 删除菜单
export function deleteMenu(id: number) {
  return request.delete<ApiResponse<void>>(`/menus/${id}`)
}

// 为角色分配菜单
export function assignMenusToRole(data: RoleMenuAssignRequest) {
  return request.post<ApiResponse<void>>('/menus/assign', data)
}
