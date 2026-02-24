import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'

export interface SystemConfig {
  key: string
  value: string
  description: string
}

// 获取所有配置
export function getConfigs() {
  return request.get<ApiResponse<SystemConfig[]>>('/config')
}

// 获取单个配置
export function getConfig(key: string) {
  return request.get<ApiResponse<SystemConfig>>(`/config/${key}`)
}

// 更新配置
export function updateConfig(key: string, value: string) {
  return request.put<ApiResponse<SystemConfig>>('/config', { key, value })
}
