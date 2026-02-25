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

// 测试AI连接
export function testAIConnection(config: Record<string, any>) {
  return request.post<ApiResponse<string>>('/config/test/ai', config).then(res => res.data?.data || '连接成功')
}

// 测试OSS连接
export function testOSSConnection(config: Record<string, any>) {
  return request.post<ApiResponse<string>>('/config/test/oss', config).then(res => res.data?.data || '连接成功')
}

// 测试邮件连接
export function testEmailConnection(config: Record<string, any>) {
  return request.post<ApiResponse<string>>('/config/test/email', config).then(res => res.data?.data || '连接成功')
}
