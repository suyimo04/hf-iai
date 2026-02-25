import type { User } from './index'

// API通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  token: string
  user: User
}

// 注册请求
export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
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
