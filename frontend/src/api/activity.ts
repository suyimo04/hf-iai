import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'

// 活动状态
export type ActivityStatus = 'DRAFT' | 'PUBLISHED' | 'ONGOING' | 'ENDED'

// 活动信息
export interface Activity {
  id: number
  title: string
  description: string
  startTime: string
  endTime: string
  location: string
  maxParticipants: number
  currentParticipants: number
  signinPoints: number
  status: ActivityStatus
  createdAt: string
  updatedAt: string
}

// 活动查询参数
export interface ActivityQuery {
  page?: number
  size?: number
  status?: ActivityStatus
  keyword?: string
}

// 创建/更新活动请求
export interface ActivityForm {
  title: string
  description: string
  startTime: string
  endTime: string
  location: string
  maxParticipants: number
  signinPoints: number
  status: ActivityStatus
}

// 报名信息
export interface ActivitySignup {
  id: number
  activityId: number
  userId: number
  username: string
  nickname: string
  signedIn: boolean
  signupTime: string
  signinTime?: string
}

// 获取活动列表
export function getActivities(params: ActivityQuery) {
  return request.get<ApiResponse<PageResponse<Activity>>>('/activities', { params })
}

// 获取活动详情
export function getActivityById(id: number) {
  return request.get<ApiResponse<Activity>>(`/activities/${id}`)
}

// 创建活动
export function createActivity(data: ActivityForm) {
  return request.post<ApiResponse<Activity>>('/activities', data)
}

// 更新活动
export function updateActivity(id: number, data: ActivityForm) {
  return request.put<ApiResponse<Activity>>(`/activities/${id}`, data)
}

// 删除活动
export function deleteActivity(id: number) {
  return request.delete<ApiResponse<void>>(`/activities/${id}`)
}

// 报名活动
export function signupActivity(id: number) {
  return request.post<ApiResponse<void>>(`/activities/${id}/signup`)
}

// 签到
export function signinActivity(id: number, userId: number) {
  return request.post<ApiResponse<void>>(`/activities/${id}/signin/${userId}`)
}

// 获取活动报名列表
export function getActivitySignups(id: number) {
  return request.get<ApiResponse<ActivitySignup[]>>(`/activities/${id}/signups`)
}
