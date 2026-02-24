import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'

// 薪酬记录
export interface Salary {
  id: number
  userId: number
  username: string
  nickname?: string
  period: string // 格式: 2024-01
  basePoints: number // 基础积分
  bonusPoints: number // 奖励积分
  deduction: number // 扣除
  totalPoints: number // 总积分
  miniCoins: number // 迷你币
  salary: number // 工资
  remark?: string // 备注
  status: 'DRAFT' | 'CONFIRMED' | 'PAID'
  createdAt: string
  updatedAt: string
}

// 薪酬编辑请求
export interface SalaryEditRequest {
  id: number
  basePoints?: number
  bonusPoints?: number
  deduction?: number
  miniCoins?: number
  remark?: string
}

// 批量保存请求
export interface SalaryBatchSaveRequest {
  period: string
  items: SalaryEditRequest[]
}

// 批量校验响应
export interface SalaryValidateResponse {
  valid: boolean
  totalSalary: number
  memberCount: number
  errors: {
    id: number
    field: string
    message: string
  }[]
}

// 薪酬列表查询参数
export interface SalaryQueryParams {
  period?: string
  status?: string
  page: number
  size: number
}

// 获取薪酬列表
export function getSalaries(params: SalaryQueryParams) {
  return request.get<ApiResponse<PageResponse<Salary>>>('/salaries', { params })
}

// 获取我的薪酬
export function getMySalaries() {
  return request.get<ApiResponse<Salary[]>>('/salaries/my')
}

// 编辑单条
export function editSalary(data: SalaryEditRequest) {
  return request.post<ApiResponse<Salary>>('/salaries/edit', data)
}

// 校验批量保存
export function validateSalaries(data: SalaryBatchSaveRequest) {
  return request.post<ApiResponse<SalaryValidateResponse>>('/salaries/validate', data)
}

// 批量保存
export function batchSaveSalaries(data: SalaryBatchSaveRequest) {
  return request.post<ApiResponse<void>>('/salaries/batch', data)
}

// 生成月度薪酬
export function generateSalary(period: string) {
  return request.post<ApiResponse<void>>('/salaries/generate', null, { params: { period } })
}
