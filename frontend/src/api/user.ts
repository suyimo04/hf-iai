import request from '@/utils/request'
import type { PageResponse, User } from '@/types'
import type { ApiResponse } from '@/types/api'

// 用户列表查询参数
export interface UserQueryParams {
  keyword?: string
  role?: string
  status?: string
  page: number
  size: number
}

// 获取用户列表
export function getUsers(params: UserQueryParams) {
  return request.get<ApiResponse<PageResponse<User>>>('/users', { params })
}

// 获取用户详情
export function getUserById(id: number) {
  return request.get<ApiResponse<User>>(`/users/${id}`)
}

// 修改用户角色
export function updateUserRole(id: number, role: string) {
  return request.put<ApiResponse<void>>(`/users/${id}/role`, { role })
}

// 修改用户状态
export function updateUserStatus(id: number, status: string) {
  return request.put<ApiResponse<void>>(`/users/${id}/status`, { status })
}

// 删除用户
export function deleteUser(id: number) {
  return request.delete<ApiResponse<void>>(`/users/${id}`)
}

// 成员流转日志
export interface MemberFlowLog {
  id: number
  userId: number
  userName: string
  fromRole: string
  toRole: string
  flowType: string
  triggerType: string
  reason: string
  operatorId: number
  operatorName: string
  createdAt: string
}

// 流转日志查询参数
export interface MemberFlowLogQueryParams {
  flowType?: string
  triggerType?: string
  startDate?: string
  endDate?: string
  page: number
  size: number
}

// 获取成员流转日志
export function getMemberFlowLogs(params: MemberFlowLogQueryParams) {
  return request.get<ApiResponse<PageResponse<MemberFlowLog>>>('/users/flow-logs', { params })
}
