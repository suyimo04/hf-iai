import request from '@/utils/request'
import type { ApiResponse, PageRequest, PageResponse } from '@/types/api'

// AI面试会话状态
export type AIInterviewStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'EXPIRED'

// 消息角色
export type MessageRole = 'USER' | 'AI' | 'SYSTEM'

// AI面试会话
export interface AIInterviewSession {
  id: number
  userId: number
  applicationId?: number
  status: AIInterviewStatus
  violationTypes: string[]
  roundCount: number
  maxRounds: number
  aiProvider?: string
  aiModel?: string
  startedAt?: string
  endedAt?: string
  createdAt: string
  updatedAt: string
}

// AI面试消息
export interface AIInterviewMessage {
  id: number
  sessionId: number
  role: MessageRole
  content: string
  sequenceNumber: number
  createdAt: string
}

// AI面试评分
export interface AIInterviewScore {
  id: number
  sessionId: number
  attitudeScore: number       // 态度分 0-25
  ruleExecutionScore: number  // 规则执行分 0-25
  emotionalControlScore: number  // 情绪控制分 0-25
  decisionRationalityScore: number  // 决策理性分 0-25
  finalScore: number          // 总分 0-100
  evaluation?: string         // AI评语
  createdAt: string
}

// AI面试统计
export interface AIInterviewStatistics {
  totalSessions: number
  completedSessions: number
  averageScore: number
  passRate: number
  todaySessions: number
}

// 会话列表查询参数
export interface AIInterviewQueryParams extends PageRequest {
  status?: AIInterviewStatus
  userId?: number
  startDate?: string
  endDate?: string
}

// 开始面试请求
export interface StartInterviewRequest {
  applicationId?: number
  violationTypes?: string[]
}

// 发送消息请求
export interface SendMessageRequest {
  content: string
}

// 违规类型定义
export interface ViolationType {
  key: string
  label: string
  description: string
}

// 违规类型列表
export const VIOLATION_TYPES: ViolationType[] = [
  { key: 'PROVOCATION', label: '引战型', description: '恶意引战、挑衅他人' },
  { key: 'INSULT', label: '辱骂型', description: '辱骂他人、言语攻击' },
  { key: 'SENSITIVE', label: '敏感话题型', description: '讨论政治、军事敏感内容' },
  { key: 'ADVERTISING', label: '广告型', description: '发布广告、机器人行为' },
  { key: 'GROUP_INVITE', label: '拉群型', description: '私自拉群' },
  { key: 'RED_PACKET_SPAM', label: '红包刷屏型', description: '红包消息刷屏' },
  { key: 'SPAM', label: '连续刷屏', description: '连续无意义刷屏' },
  { key: 'NO_NICKNAME', label: '不改群名片', description: '拒绝修改群名片格式' },
  { key: 'DEFIANCE', label: '顶撞管理', description: '顶撞管理员、不服从管理' }
]

// AI面试状态映射
export const AI_INTERVIEW_STATUS_MAP: Record<AIInterviewStatus, { label: string; type: 'warning' | 'info' | 'primary' | 'success' | 'danger' }> = {
  PENDING: { label: '等待中', type: 'warning' },
  IN_PROGRESS: { label: '进行中', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  EXPIRED: { label: '已过期', type: 'info' }
}

// ==================== API 方法 ====================

// 开始AI面试
export function startAIInterview(data?: StartInterviewRequest) {
  return request.post<ApiResponse<AIInterviewSession>>('/ai-interviews/start', data || {})
}

// 获取会话详情
export function getAIInterviewSession(sessionId: number) {
  return request.get<ApiResponse<AIInterviewSession>>(`/ai-interviews/sessions/${sessionId}`)
}

// 获取会话消息列表
export function getSessionMessages(sessionId: number) {
  return request.get<ApiResponse<AIInterviewMessage[]>>(`/ai-interviews/sessions/${sessionId}/messages`)
}

// 发送消息（用户回复）
export function sendMessage(sessionId: number, data: SendMessageRequest) {
  return request.post<ApiResponse<AIInterviewMessage>>(`/ai-interviews/sessions/${sessionId}/messages`, data)
}

// 结束面试
export function endAIInterview(sessionId: number) {
  return request.post<ApiResponse<AIInterviewSession>>(`/ai-interviews/sessions/${sessionId}/end`)
}

// 获取面试评分
export function getAIInterviewScore(sessionId: number) {
  return request.get<ApiResponse<AIInterviewScore>>(`/ai-interviews/sessions/${sessionId}/score`)
}

// 获取会话列表（管理员）
export function getAIInterviewSessions(params: AIInterviewQueryParams) {
  return request.get<ApiResponse<PageResponse<AIInterviewSession>>>('/ai-interviews/sessions', { params })
}

// 获取我的面试记录
export function getMyAIInterviews() {
  return request.get<ApiResponse<AIInterviewSession[]>>('/ai-interviews/my-sessions')
}

// 获取AI面试统计数据
export function getAIInterviewStatistics() {
  return request.get<ApiResponse<AIInterviewStatistics>>('/ai-interviews/statistics')
}

// 获取违规类型列表（从后端获取，如果需要动态配置）
export function getViolationTypes() {
  return request.get<ApiResponse<ViolationType[]>>('/ai-interviews/violation-types')
}

// 根据违规类型key获取标签
export function getViolationLabel(key: string): string {
  const type = VIOLATION_TYPES.find(t => t.key === key)
  return type?.label || key
}

// 根据违规类型key获取描述
export function getViolationDescription(key: string): string {
  const type = VIOLATION_TYPES.find(t => t.key === key)
  return type?.description || ''
}
