import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'

// 操作日志
export interface OperationLog {
  id: number
  userId: number
  username: string
  category?: string
  action: string
  targetType: string
  targetId: number
  detail: string
  ip: string
  createdAt: string
}

// 角色变更日志
export interface RoleChangeLog {
  id: number
  userId: number
  username: string
  fromRole: string
  toRole: string
  reason?: string
  changedBy: number
  changedByName?: string
  ip?: string
  createdAt: string
}

// 日志查询参数
export interface LogQueryParams {
  userId?: number
  username?: string
  category?: string
  action?: string
  targetType?: string
  startDate?: string
  endDate?: string
  page: number
  size: number
}

// 角色变更日志查询参数
export interface RoleChangeLogQueryParams {
  username?: string
  fromRole?: string
  toRole?: string
  startDate?: string
  endDate?: string
  page: number
  size: number
}

// 获取日志列表
export function getLogs(params: LogQueryParams) {
  return request.get<ApiResponse<PageResponse<OperationLog>>>('/logs', { params })
}

// 获取角色变更日志
export function getRoleChangeLogs(params: RoleChangeLogQueryParams) {
  return request.get<ApiResponse<PageResponse<RoleChangeLog>>>('/logs/role-change', { params })
}
