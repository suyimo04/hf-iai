import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'

// 报名表单数据
export interface ApplicationFormData {
  name: string
  phone: string
  email: string
  age?: number
  introduction: string
  reason: string
  skills?: string
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  role: string
  status: string
  createdAt: string
}

// 报名记录
export interface Application {
  id: number
  user?: UserInfo
  userId?: number
  formData: ApplicationFormData
  status: ApplicationStatus
  reviewer?: UserInfo
  reviewComment?: string
  createdAt: string
  updatedAt: string
}

// 报名状态（与后端 ApplicationStatus 枚举一致）
export type ApplicationStatus = 'PENDING' | 'REVIEWING' | 'INTERVIEW' | 'PASSED' | 'REJECTED' | 'INTERN' | 'CONVERTED'

// 报名状态映射
export const APPLICATION_STATUS_MAP: Record<ApplicationStatus, { label: string; type: 'warning' | 'info' | 'primary' | 'success' | 'danger' }> = {
  PENDING: { label: '待审核', type: 'warning' },
  REVIEWING: { label: '审核中', type: 'info' },
  INTERVIEW: { label: '面试中', type: 'primary' },
  PASSED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  INTERN: { label: '实习中', type: 'primary' },
  CONVERTED: { label: '已转正', type: 'success' }
}

// 分页参数
export interface ApplicationListParams {
  status?: ApplicationStatus
  keyword?: string
  page?: number
  size?: number
}

// 分页响应
export interface ApplicationListResponse {
  content: Application[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 提交报名
export function submitApplication(data: { formData: ApplicationFormData }) {
  return request.post<ApiResponse<Application>>('/applications', data)
}

// 获取我的报名
export function getMyApplication() {
  return request.get<ApiResponse<Application | null>>('/applications/my')
}

// 获取报名列表（管理员）
export function getApplications(params: ApplicationListParams) {
  return request.get<ApiResponse<ApplicationListResponse>>('/applications', { params })
}

// 获取报名详情
export function getApplicationById(id: number) {
  return request.get<ApiResponse<Application>>(`/applications/${id}`)
}

// 审核报名
export function reviewApplication(id: number, data: { status: ApplicationStatus; comment?: string }) {
  return request.post<ApiResponse<Application>>(`/applications/${id}/review`, data)
}
