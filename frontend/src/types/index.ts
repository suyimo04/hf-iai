// 角色枚举
export type Role = 'ADMIN' | 'LEADER' | 'VICE_LEADER' | 'MEMBER' | 'INTERN' | 'APPLICANT'

// 用户信息
export interface User {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  role: Role
  status: string
  createdAt: string
}

// 分页请求
export interface PageRequest {
  page: number
  size: number
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
