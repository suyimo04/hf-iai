import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'

export interface SystemConfig {
  key: string
  value: string
  description: string
}

export interface TestResult {
  success: boolean
  message?: string
}

export interface ConfigUpdateRequest {
  group: string
  configs: Record<string, any>
}

// 获取所有配置
export function getConfigs() {
  return request.get<ApiResponse<SystemConfig[]>>('/config')
}

// 获取单个配置
export function getConfig(key: string) {
  return request.get<ApiResponse<SystemConfig>>(`/config/${key}`)
}

// 获取配置（按分组）
export function getConfigByGroup(group: string) {
  return request.get<ApiResponse<Record<string, any>>>(`/config/group/${group}`)
}

// 更新配置（单个）
export function updateConfigByKey(key: string, value: string) {
  return request.put<ApiResponse<SystemConfig>>('/config', { key, value })
}

// 更新配置（按分组批量）
export function updateConfig(data: ConfigUpdateRequest) {
  return request.put<ApiResponse<void>>('/config/batch', data)
}

// 测试AI连接
export function testAIConnection(config: Record<string, any>) {
  return request.post<ApiResponse<TestResult>>('/config/test/ai', config)
}

// 测试OSS连接
export function testOSSConnection(config: Record<string, any>) {
  return request.post<ApiResponse<TestResult>>('/config/test/oss', config)
}

// 测试邮件连接
export function testEmailConnection(config: Record<string, any>) {
  return request.post<ApiResponse<TestResult>>('/config/test/email', config)
}
