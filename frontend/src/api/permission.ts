import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'

// 权限
export interface Permission {
  id: number
  name: string
  code: string
  menuId?: number
  menuName?: string
  description?: string
  createdAt?: string
}

// 创建权限请求
export interface PermissionCreateRequest {
  name: string
  code: string
  menuId?: number
  description?: string
}

// 角色权限分配请求
export interface RolePermissionAssignRequest {
  role: string
  permissionIds: number[]
}

// 获取所有权限
export function getPermissions() {
  return request.get<ApiResponse<Permission[]>>('/permissions')
}

// 获取角色的权限列表
export function getRolePermissions(role: string) {
  return request.get<ApiResponse<Permission[]>>(`/permissions/role/${role}`)
}

// 创建权限
export function createPermission(data: PermissionCreateRequest) {
  return request.post<ApiResponse<Permission>>('/permissions', data)
}

// 删除权限
export function deletePermission(id: number) {
  return request.delete<ApiResponse<void>>(`/permissions/${id}`)
}

// 为角色分配权限
export function assignPermissionsToRole(data: RolePermissionAssignRequest) {
  return request.post<ApiResponse<void>>('/permissions/assign', data)
}
