import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'

// 看板统计数据
export interface DashboardStats {
  userCount: number
  memberCount: number
  applicationCount: number
  pendingApplicationCount: number
  activityCount: number
  activeActivityCount: number
  totalPoints: number
  monthlyPoints: number
}

// 趋势数据点
export interface TrendData {
  date: string
  value: number
}

// 获取统计数据
export function getDashboardStats() {
  return request.get<ApiResponse<DashboardStats>>('/dashboard')
}

// 获取用户趋势数据
export function getUserTrend(days: number = 7) {
  return request.get<ApiResponse<TrendData[]>>('/dashboard/trend/user', {
    params: { days }
  })
}

// 获取报名趋势数据
export function getApplicationTrend(days: number = 7) {
  return request.get<ApiResponse<TrendData[]>>('/dashboard/trend/application', {
    params: { days }
  })
}

// 获取积分趋势数据
export function getPointsTrend(days: number = 7) {
  return request.get<ApiResponse<TrendData[]>>('/dashboard/trend/points', {
    params: { days }
  })
}
